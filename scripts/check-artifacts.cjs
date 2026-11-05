const {chromium}=require(process.env.PLAYWRIGHT_MODULE||'playwright');
const assert=require('node:assert/strict'),fs=require('node:fs'),path=require('node:path'),crypto=require('node:crypto');
(async()=>{
 const base=(process.env.DEMO_URL||'https://expense-tracker-demo.hummingmole.chatgpt.site').replace(/\/$/,'');
 const expected=JSON.parse(fs.readFileSync('docs/verification/evidence/release.json','utf8')).assets[0];
 const browser=await chromium.launch({headless:true,...(process.env.CHROMIUM_PATH?{executablePath:process.env.CHROMIUM_PATH}:{})});
 const page=await browser.newPage({viewport:{width:390,height:844}}),errors=[];page.on('pageerror',e=>errors.push(e.message));
 try{
  await page.goto(base+'/native.html');await page.getByRole('heading',{name:'Native Android screens',exact:true}).waitFor();assert.equal(await page.locator('.capture').count(),38);assert(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth));
  await page.locator('.capture img').first().evaluate(img=>img.decode());
  const links=await page.locator('.capture').evaluateAll(nodes=>nodes.map(a=>a.href));
  for(let i=0;i<links.length;i+=6){await Promise.all(links.slice(i,i+6).map(async url=>{const response=await page.request.head(url);assert.equal(response.status(),200,url);assert(response.headers()['content-type'].includes('image/png'),url)}))}
  fs.mkdirSync('docs/verification/public',{recursive:true});await page.screenshot({path:'docs/verification/public/09-native-gallery.png'});assert.deepEqual(errors,[]);
  const apkUrl=base+'/expense-tracker-debug.apk',response=await fetch(apkUrl);assert.equal(response.status,200);const hash=crypto.createHash('sha256');let size=0;for await(const chunk of response.body){hash.update(chunk);size+=chunk.length}const digest='sha256:'+hash.digest('hex');assert.equal(size,expected.size);assert.equal(digest,expected.digest);
  const result={gallery:base+'/native.html',native_images:38,apk:apkUrl,size,digest,mobile_overflow:false,browser_errors:errors};fs.writeFileSync('docs/verification/evidence/public-artifacts.json',JSON.stringify(result,null,2)+'\n');console.log('PASS: 38 public native PNGs, mobile gallery, zero page errors and byte-for-byte public APK checksum.');console.log(JSON.stringify(result));
 }finally{await browser.close()}
})().catch(e=>{console.error(e);process.exitCode=1});
