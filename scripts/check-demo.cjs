const {chromium}=require(process.env.PLAYWRIGHT_MODULE||'playwright');
const assert=require('node:assert/strict'),path=require('node:path'),fs=require('node:fs/promises');
(async()=>{
 const out=path.resolve(process.env.SCREENSHOT_DIR||'docs/verification/browser');await fs.mkdir(out,{recursive:true});
 const browser=await chromium.launch({headless:true,...(process.env.CHROMIUM_PATH?{executablePath:process.env.CHROMIUM_PATH}:{})});
 const page=await browser.newPage({viewport:{width:1440,height:1000}}),errors=[];page.on('pageerror',e=>errors.push(e.message));
 const nav=async name=>page.getByRole('navigation').getByRole('button',{name,exact:true}).click();
 const shot=async name=>page.screenshot({path:path.join(out,name+'.png'),fullPage:true});
 try{
  await page.goto(process.env.DEMO_URL||require('node:url').pathToFileURL(path.resolve('docs/demo/index.html')).href);
  await page.getByRole('heading',{name:'Your month, at a glance'}).waitFor();await shot('01-overview');
  await page.getByRole('button',{name:'Add transaction',exact:true}).click();await shot('02-transaction-form');
  await page.getByLabel('Amount',{exact:true}).fill('Infinity');await page.getByRole('button',{name:'Save transaction',exact:true}).click();await page.getByRole('alert').filter({hasText:'Enter a valid positive amount'}).waitFor();
  await page.getByLabel('Amount',{exact:true}).fill('19.95');await page.getByLabel('Note',{exact:true}).fill('Browser regression fixture');await page.getByRole('button',{name:'Save transaction',exact:true}).click();
  await nav('Transactions');await page.getByLabel('Search transactions').fill('Browser regression fixture');assert.equal(await page.locator('.list-button').count(),1);
  await page.locator('.list-button').click();await page.getByLabel('Amount',{exact:true}).fill('24.50');await page.getByRole('button',{name:'Save transaction',exact:true}).click();await page.locator('.value').filter({hasText:'−$24.50'}).waitFor();await shot('03-transactions');
  const download=page.waitForEvent('download');await page.getByRole('button',{name:'Export CSV',exact:true}).click();assert.equal((await download).suggestedFilename(),'synthetic-expenses.csv');
  await page.locator('.list-button').click();page.once('dialog',d=>d.accept());await page.getByRole('button',{name:'Delete transaction',exact:true}).click();assert.equal(await page.locator('.list-button').count(),0);
  await nav('Budgets');await page.getByLabel('Monthly limit').fill('0');await page.getByRole('button',{name:'Save budget',exact:true}).click();await page.getByRole('alert').filter({hasText:'Enter a valid positive amount'}).waitFor();await page.getByLabel('Monthly limit').fill('400');await page.getByRole('button',{name:'Save budget',exact:true}).click();await shot('04-budgets');
  await nav('Recurring');await page.locator('#recurring-amount').fill('25');await page.locator('#recurring-note').fill('Synthetic subscription');await page.getByRole('button',{name:'Save recurring entry',exact:true}).click();await page.getByRole('button',{name:'Pause',exact:true}).click();await page.getByRole('button',{name:'Resume',exact:true}).waitFor();await shot('05-recurring');
  await nav('Converter');await page.getByRole('button',{name:'Calculate sample',exact:true}).click();assert.equal(await page.getByRole('status').textContent(),'€92.00');await page.locator('#convert-amount').fill('-1');await page.getByRole('button',{name:'Calculate sample',exact:true}).click();assert.equal(await page.getByRole('status').textContent(),'Enter a valid non-negative amount');await shot('06-converter');
  await nav('Settings');await page.getByLabel('Selected currency').selectOption('EUR');await shot('07-settings');await nav('Overview');await page.getByText('-€65.00',{exact:true}).waitFor();
  await page.getByRole('button',{name:'Reset sample data',exact:true}).click();await page.setViewportSize({width:390,height:844});assert(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth));await shot('08-mobile-overview');
  for(const name of ['Transactions','Budgets','Recurring','Converter','Settings']){await nav(name);assert(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),`Overflow on ${name}`)}
  assert.deepEqual(errors,[]);console.log('PASS: add, invalid amount, edit, search, delete, CSV download, budget validation/save, recurring pause, converter validation, currency isolation, reset, all mobile screens and zero browser errors.');
 }finally{await browser.close()}
})().catch(e=>{console.error(e);process.exitCode=1});


