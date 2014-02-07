Main View

Scenario: date and week day is current
Given the main view is open
Then in the main view the date should be current
And in the main view the week day should be current

Scenario: Main view should have foods when they are provided
Given the main view is open
And the following foods are provided: fi/nottingham/mobilefood/test_foods.table
Then in the main view we should have the following foods: fi/nottingham/mobilefood/test_foods.table

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

