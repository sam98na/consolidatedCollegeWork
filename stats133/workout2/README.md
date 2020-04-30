<h2>WORKOUT 2:</h2> Visualization of Different Investment Scenarios

The second major project of Fall 2019 Stats 133. Involved creating a Shiny app that could take in user
input to visualize three different investment scenarios; a high yields savings account, a US Fixed
Income Index Fund, and a US Equity Index Fund. 


---



The inputs for the user included:
- initial contribution
- annual contribution
- annual growth rate
- annual return rate (for each investment scenario)
- annual volatility rate (for each investment scenario)
- random seed (for testing purposes)
- number of years
- whether or not the resulting plot was facetted or not

The resulting plot was either three different mini-plots, facetted by the type of investment, or
one plot with three different lines, colored by the type of investment. 


---



The filestructure for this projects can be described as:
- app.r: the project code
- workout2-sam-na.Rmd: testing file (done in an interactive environment to make debugging easier)
- workout2-sam-na.html: knitted form of workout2-sam-na.Rmd
