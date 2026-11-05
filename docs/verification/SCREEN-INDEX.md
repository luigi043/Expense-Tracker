# Screen index

Native images are real emulator captures from the Android application, collected by `NativeWalkthroughTest`. Only synthetic records are used. The numbered filenames identify evidence; the walkthrough sometimes revisits a screen, so filename order is not an exact timestamp sequence.

## Native Android

| Image | Screen or state | How to reach it |
| --- | --- | --- |
| [00](native/00-initial-state.png) | Initial composition diagnostic | Launch the app and wait for Compose |
| [01](native/01-dashboard-empty.png) | Empty dashboard | Fresh synthetic data / Home |
| [02](native/02-history-empty.png) | Empty history | History navigation |
| [03](native/03-budget-empty.png) | Empty budgets | Budget navigation |
| [04](native/04-budget-dialog.png) | Set Monthly Budget dialog | Add budget |
| [05](native/05-budget-validation.png) | Invalid budget amount | Save the empty budget dialog |
| [06](native/06-budget-saved.png) | Saved category budget | Save a synthetic USD 300 limit |
| [07](native/07-add-transaction.png) | Add transaction | Home → Add Transaction |
| [08](native/08-transaction-validation.png) | Invalid transaction amount | Save without a positive amount |
| [09](native/09-date-picker.png) | Transaction date picker | Pick date on the transaction form |
| [10](native/10-transaction-currency-picker.png) | Transaction currency dialog | Change currency on the form |
| [11](native/11-dashboard-populated.png) | Dashboard with expense charts | Save the synthetic USD 42.50 transaction |
| [12](native/12-history-populated.png) | Populated transaction history | History after saving |
| [13](native/13-history-filters.png) | History filters | Filter action |
| [14](native/14-edit-transaction.png) | Edit transaction | Tap Synthetic client lunch |
| [15](native/15-delete-confirmation.png) | Transaction delete confirmation | Delete action on the edit screen |
| [16](native/16-export-menu.png) | CSV/PDF export menu | Export action in history |
| [17](native/17-settings.png) | Settings | Settings navigation |
| [18](native/18-base-currency-picker.png) | Base-currency selection | Settings → Change |
| [19](native/19-currency-converter.png) | Currency converter | Settings → Currency Converter |
| [20](native/20-converter-validation.png) | Invalid converter input | Enter a negative amount |
| [21](native/21-recurring-empty.png) | Empty recurring schedules | Settings → Recurring Expenses |
| [22](native/22-recurring-dialog.png) | Add Recurring Entry dialog | Add recurring |
| [23](native/23-recurring-validation.png) | Invalid recurring amount | Save the empty recurring dialog |
| [24](native/24-recurring-saved.png) | Saved recurring schedule | Save the synthetic USD 25 template |
| [25](native/25-budget-spending.png) | Budget with actual spending | Budget after editing the expense to USD 45 |
| [26](native/26-history-from-date.png) | History start-date picker | Filters → From date |
| [27](native/27-history-to-date.png) | History end-date picker | Filters → To date |
| [28](native/28-converter-currency-picker.png) | Converter currency selection | Converter → To |
| [29](native/29-converter-offline-result.png) | Offline same-currency result | Convert USD 12 to USD |
| [30](native/30-converter-unconfigured.png) | Missing API configuration error | Convert USD to EUR with the placeholder key |
| [31](native/31-recurring-paused.png) | Paused recurring schedule | Turn off the template's active switch |
| [32](native/32-recurring-delete-confirmation.png) | Schedule deletion confirmation | Delete recurring entry |
| [33](native/33-settings-dark.png) | Dark appearance | Settings → Dark Mode |
| [34](native/34-history-after-recreation.png) | Saved transaction after activity recreation | Recreate the activity and reopen History |
| [35](native/35-transaction-deleted.png) | History after actual deletion | Confirm transaction deletion |
| [36](native/36-income-form.png) | Income transaction form | Add Transaction → Income and enter synthetic salary |
| [37](native/37-income-dashboard.png) | Dashboard with income | Save the synthetic USD 1,000 salary |

`initial-screen.png` is an earlier settled-launch diagnostic outside the ordered walkthrough. It is also an actual Android screenshot.

## Browser companion

These images demonstrate JavaScript browser behavior, not native Android behavior. The same eight named captures are in `browser/` for local files and `public/` for the published page.

| Image name | Screen/state | Action |
| --- | --- | --- |
| `01-overview.png` | Overview with synthetic data | Open the demo |
| `02-transaction-form.png` | Transaction dialog | Add transaction |
| `03-transactions.png` | Filtered, edited history | Search and edit the regression fixture |
| `04-budgets.png` | Budgets | Validate and save a category limit |
| `05-recurring.png` | Recurring schedules | Save and pause a synthetic subscription |
| `06-converter.png` | Invalid converter input | Enter a negative amount |
| `07-settings.png` | Settings | Select EUR |
| `08-mobile-overview.png` | Phone-width overview | Reset sample data at 390px width |

`public/09-native-gallery.png` captures the phone-width public gallery page. It is a browser page displaying the real native captures, not another Android screen. The full native PNGs are linked individually at the [public gallery](https://expense-tracker-demo.hummingmole.chatgpt.site/native.html).

## External states and coverage limits

The app has eight navigation destinations, all listed above. The converter has no verified live-rate success state because no real API key was supplied. The offline result and missing-configuration state are covered. CSV/PDF creation and content URI access are covered by a native integration test; an external viewer's UI is not part of this app and depends on installed software. Android-controlled background timing is not represented by a fabricated screenshot. The recurring processor is tested directly against an isolated in-memory Room database.

Calendar dates, categories and currencies are data choices within these screens rather than separate navigation destinations. Infinite combinations of those choices are not claimed as exhaustive test coverage. See [RESULTS.md](RESULTS.md) for the actual final test status.
