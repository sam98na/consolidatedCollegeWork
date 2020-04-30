#=======================================
# Title: EDA Script
# Description: 
#   Script for the purpose of exploring
#   relationships and trends found in
#   the ibtracs storm dataset
# Input: data file (modified)
#             ibtracs-2010-2015.csv
# Output(s): plots
# Author: Sam Na
# Date Due: 10/18/2019
#=======================================

library(dplyr)
library(readr)
library(ggplot2)
library(maps)

setwd("~/workout1/code")
colnm <- c("serial_num", "season", "num", "basin", 
           "sub-basin", "name", "iso_time", "nature",
           "latitude", "longitude", "wind", "press")
coltyp <- c("character", "integer", "character",
            "factor", "character", "character",
            "character", "character", "real",
            "real", "real", "real")
dat <- read.csv("../data/ibtracs-2010-2015.csv", 
         col.names = colnm, colClasses = coltyp,
         na = c("-999.", "0.0", "-1.0"))
sink(file = "../output/data-summary.txt")
summary(dat)
sink()


#=================================================

world <- borders("world", colour="gray50", fill="gray50")
ggplot() + world + geom_point(aes(x=dat$longitude, y=dat$latitude,
                                        color = dat$nature) , size=1) 
ggsave("../images/map-all-storms.pdf", device = "pdf")
ggsave("../images/map-all-storms.png", device = "png")

epna <- filter(dat, as.integer(basin) < 3)
world <- borders("world", colour="gray50", fill="gray50")
ggplot(epna, aes(x=epna$longitude, y=epna$latitude,
      color = epna$basin)) + world + geom_point(size=1) + facet_wrap(vars(season))

ggsave("../images/map-ep-na-storms-by-year.pdf", device = "pdf")
ggsave("../images/map-ep-na-storms-by-year.png", device = "png")

datmonth <-  dat
datmonth$month <- as.integer(substr(dat$iso_time, 6, 7))

epnamonth <- filter(datmonth, as.integer(basin) < 3)
world <- borders("world", colour="gray50", fill="gray50")
ggplot(epnamonth, aes(x=epnamonth$longitude, y=epnamonth$latitude,
    color = epnamonth$basin)) + world + geom_point(size=1) + facet_wrap(vars(month))

ggsave("../images/map-ep-na-storms-by-month.pdf", device = "pdf")
ggsave("../images/map-ep-na-storms-by-month.png", device = "png")


thirdfactor <- filter(datmonth, as.integer(basin) == 3)
world <- borders("world", colour="gray50", fill="gray50")
ggplot(thirdfactor, aes(x=thirdfactor$longitude, y=thirdfactor$latitude,
          color = thirdfactor$basin)) + world + geom_point(size=1) + facet_wrap(vars(month))


ggsave("../images/map-ni-storms-by-month.png", device = "png")

four <- filter(datmonth, as.integer(basin) == 4)
world <- borders("world", colour="gray50", fill="gray50")
ggplot(four, aes(x=four$longitude, y=four$latitude,
      color = four$basin)) + world + geom_point(size=1) + facet_wrap(vars(month))


ggsave("../images/map-sa-storms-by-month.png", device = "png")

five <- filter(datmonth, as.integer(basin) == 5)
world <- borders("world", colour="gray50", fill="gray50")
ggplot(five, aes(x=five$longitude, y=five$latitude,
                 color = five$basin)) + world + geom_point(size=1) + facet_wrap(vars(month))


ggsave("../images/map-si-storms-by-month.png", device = "png")

six <- filter(datmonth, as.integer(basin) == 6)
world <- borders("world", colour="gray50", fill="gray50")
ggplot(six, aes(x=six$longitude, y=six$latitude,
                 color = six$basin)) + world + geom_point(size=1) + facet_wrap(vars(month))


ggsave("../images/map-sp-storms-by-month.png", device = "png")

seven <- filter(datmonth, as.integer(basin) == 7)
world <- borders("world", colour="gray50", fill="gray50")
ggplot(seven, aes(x=seven$longitude, y=seven$latitude,
                color = seven$basin)) + world + geom_point(size=1) + facet_wrap(vars(month))


ggsave("../images/map-wp-storms-by-month.png", device = "png")

world <- borders("world", colour="gray50", fill="gray50")
ggplot(dat, aes(x=dat$longitude, y=dat$latitude,
                 color = dat$basin)) + world + geom_point(size=1) + facet_wrap(vars(season))

ggsave("../images/map-all-storms-by-year.png", device = "png")

sisp <- filter(datmonth, as.integer(basin) == 5 | as.integer(basin) == 6)
world <- borders("world", colour="gray50", fill="gray50")
ggplot(sisp, aes(x=sisp$longitude, y=sisp$latitude,
                 color = sisp$basin)) + world + geom_point(size=1) + facet_wrap(vars(month))


ggsave("../images/map-si-sp-storms-by-month.png", device = "png")

world <- borders("world", colour="gray50", fill="gray50")
ggplot(datmonth, aes(x=datmonth$longitude, y=datmonth$latitude,
                 color = datmonth$nature)) + world + geom_point(size=1) + facet_wrap(vars(month))


ggsave("../images/map-nature-storms-by-month.png", device = "png")









