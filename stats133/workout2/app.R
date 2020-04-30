#
# This is a Shiny web application. You can run the application by clicking
# the 'Run App' button above.
#
# Find out more about building applications with Shiny here:
#
#    http://shiny.rstudio.com/
#

library(shiny)
library(ggplot2)

# Define UI for application that draws a histogram
ui <- fluidPage(

    # Application title
    titlePanel("Investing Scenario Visualizer"),
    # Sidebar with a slider input for number of bins 
    fluidRow(
        column(3, 
            sliderInput("initial",
                        "Initial Amount:",
                        min = 0,
                        max = 10000,
                        value = 1000),
            sliderInput("annualcont",
                        "Annual Contribution:",
                        min = 0,
                        max = 5000,
                        value = 200),
            sliderInput("annualgrowth",
                        "Annual Growth Rate (%):",
                        min = 0,
                        max = 20,
                        value = 2,
                        step = 0.1)),
        column(3,
            sliderInput("highyield",
                        "High Yield Rate (%):",
                        min = 0,
                        max = 20,
                        value = 2,
                        step = 0.1),
            sliderInput("fixedincome",
                        "Fixed Income Rate (%):",
                        min = 0,
                        max = 20,
                        value = 5,
                        step = 0.1),
            sliderInput("usequity",
                        "US Equity Rate (%):",
                        min = 0,
                        max = 20,
                        value = 10,
                        step = 0.1)),
        column(3,
            sliderInput("highyieldvol",
                        "High Yield Volatility Rate (%):",
                        min = 0,
                        max = 20,
                        value = 0.1,
                        step = 0.1),
            sliderInput("fixedincomevol",
                        "Fixed Income Volatility Rate (%):",
                        min = 0,
                        max = 20,
                        value = 4.5,
                        step = 0.1),
            sliderInput("usequityvol",
                        "US Equity Volatility Rate (%):",
                        min = 0,
                        max = 20,
                        value = 15,
                        step = 0.1)),
        column(3,
            sliderInput("year",
                        "Years",
                        min = 0,
                        max = 50,
                        step = 1,
                        value = 20),
            numericInput("seed",
                         "Random Seed",
                         value = 12345),
            selectInput("facet",
                        "Facet?",
                        choices = c("Yes", "No"),
                        selected = "Yes"))
        ),
    hr(),
    h2("Visualization of Three Investing Scenarios"),
    plotOutput("distPlot")
    )

# Define server logic required to draw a histogram
server <- function(input, output) {

    output$distPlot <- renderPlot({
        #' @title Annual Rate of Return Function
        #' @description Calculates a randomized annual rate of return
        #' @param ref Reference Annual Rate
        #' @param vol Volatility of given Annual Rate
        #' @param seed Seed for random number generation
        #' @return an annual rate of return
        
        randannual <- function(ref, vol, seed){
            return(rnorm(1, mean = ref, sd = vol))
        }
        
        #' @title Single Investment Calculator Function
        #' @description Calculates the total value of a single type of investment given parameters
        #' @param ref Reference Annual Rate
        #' @param vol Volatility of given Annual Rate
        #' @param seed Seed for random number generation
        #' @param years Number of years
        #' @param initial Initial Investment
        #' @param growth Growth Rate
        #' @param cont Annual Contribution
        #' @return a vector containing the investment amount per year
        
        invest <- function(ref, vol, seed, years = 1, initial, growth, cont){
            finalvector <- rep(0, years)
            for(i in 1:years){
                ratereturn <- randannual(ref, vol, seed)
                if(i == 1){
                    finalvector[i] <- initial*(1+ratereturn)+cont*(1+growth)^(i-1)
                } else {
                    finalvector[i] <- finalvector[i-1]*(1+ratereturn)+cont*(1+growth)^(i-1)
                }
            }
            return(finalvector)
        }
        
        #' @title Collated Investments Function
        #' @description Creates a dataframe of all three types of investments given parameters for each
        #' @param refhy Reference Annual Rate (High Yield)
        #' @param volhy Volatility of given High Yield Annual Rate
        #' @param reffixed Reference Annual Rate (Fixed Income)
        #' @param volfixed Volatility of given Fixed Income Annual Rate
        #' @param refequity Reference Annual Rate (Equity)
        #' @param volequity Volatility of given Equity Annual Rate
        #' @param seed Seed for random number generation
        #' @param years Number of years
        #' @param initial Initial Investment
        #' @param growth Growth Rate
        #' @param cont Annual Contribution
        #' @return a dataframe with columns for year, all three types of investments, and
        #' a factor column (to signify which type of investment it is)
        
        collate <- function(refhy, volhy, reffixed, 
            volfixed, refequity, volequity, seed, years, 
            initial, growth, cont){
            set.seed(seed)
            highyield <- invest(refhy, volhy, seed, years, initial, growth, cont)
            fixed <- invest(reffixed, volfixed, seed, years, initial, growth, cont)
            equity <- invest(refequity, volequity, seed, years, initial, growth, cont)
            all <- c(highyield, fixed, equity)
            year <- c(1:years, 1:years, 1:years)
            fact <- c(rep("high", years), rep("fixed", years), rep("equity", years))
            finaldf <- data.frame(year, all, fact)
            return(finaldf)
        }
        
        
        #Create dataframe to use for plotting
        df <- collate(input$highyield/100, input$highyieldvol/100, 
        input$fixedincome/100, input$fixedincomevol/100, input$usequity/100,
        input$usequityvol/100, input$seed, input$year, input$initial,
        input$annualgrowth/100, input$annualcont)
        
        
        if(input$facet == "Yes"){
            ggplot(df, aes(x = year, 
                y = all, color = fact)) + 
                geom_point() + geom_line()+
                facet_wrap(df$fact) + 
                ylab("Amount (in $)") + 
                xlab("Years") + 
                ggtitle("Facetted")
        } else {
            ggplot(df, aes(x = year, 
                y = all, color = fact)) + 
                geom_line() + geom_point() + 
                ylab("Amount (in $)") + 
                xlab("Years") + 
                ggtitle("Non-Facetted")
        }
        })
}

# Run the application 
shinyApp(ui = ui, server = server)
