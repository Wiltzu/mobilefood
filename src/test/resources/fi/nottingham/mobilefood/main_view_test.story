Main View

Scenario: current date's foods are visible in the main view
Given the main view is open
When system date is set to '6.2.2014'
Then in the main view the date should be '6.2.2014'
And in the main view we should have foods

Scenario: restaurants can be filtered

Scenario: restaurants can be set as favourites

Scenario: restaurants are ordered by name
Given in the main view foods are visible
Then in the main view restaurants should be ordered by name

Scenario: date can be changed
When in the main view user changes date to tomorrow
Then in the main view date should be changed

Scenario: Application menu should on the top of the view
Then the application menu should be visible

