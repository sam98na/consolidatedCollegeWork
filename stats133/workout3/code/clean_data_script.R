#######################################
## Made for Project Purposes, Actual ##
## Code run in RMD file in "rawdata" ##
## folder. This code will not work if##
## run, pls do not run               ##
#######################################

library(xml2)
library(rvest)
library(dplyr)

scholar <- "https://scholar.google.com/citations?user="
stefanstart <- "B5skt-cAAAAJ&hl=en&oi=ao&cstart="
moernerstart <- "upREfesAAAAJ&hl=en&oi=ao&cstart="
urlend <- "00&pagesize=100"
for(i in 0:7){
  stefan_url <- paste0(scholar, paste0(stefanstart, i, urlend))
  moerner_url <- paste0(scholar, paste0(moernerstart, i, urlend))
  download.file(moerner_url,
                paste0("william_moerner_google_scholar", i, ".html"))
  download.file(stefan_url,
                paste0("stefan_hell_google_scholar", i, ".html"))
}


#Reading in all html files
stefandata0 <- read_html("stefan_hell_google_scholar0.html")
stefandata1 <- read_html("stefan_hell_google_scholar1.html")
stefandata2 <- read_html("stefan_hell_google_scholar2.html")
stefandata3 <- read_html("stefan_hell_google_scholar3.html")
stefandata4 <- read_html("stefan_hell_google_scholar4.html")
stefandata5 <- read_html("stefan_hell_google_scholar5.html")
stefandata6 <- read_html("stefan_hell_google_scholar6.html")
stefandata7 <- read_html("stefan_hell_google_scholar7.html")
moernerdata0 <- read_html("william_moerner_google_scholar0.html")
moernerdata1 <- read_html("william_moerner_google_scholar1.html")
moernerdata2 <- read_html("william_moerner_google_scholar2.html")
moernerdata3 <- read_html("william_moerner_google_scholar3.html")
moernerdata4 <- read_html("william_moerner_google_scholar4.html")
moernerdata5 <- read_html("william_moerner_google_scholar5.html")
moernerdata6 <- read_html("william_moerner_google_scholar6.html")
moernerdata7 <- read_html("william_moerner_google_scholar7.html")

stefan_table <- html_table(stefandata1)
moerner_table <- html_table(moernerdata1)

#Names
html_nodes(stefandata1, "#gsc_prf_in")%>%html_text()
html_nodes(moernerdata1, "#gsc_prf_in")%>%html_text()

#Institutions
html_nodes(stefandata1, ".gsc_prf_il")[1]%>%html_text()
html_nodes(moernerdata1, ".gsc_prf_il")[1]%>%html_text()


#html_nodes(stefandata1, ".gs_gray")[1] %>% html_text()
#html_nodes(get(paste0("stefandata", "1")), ".gsc_a_t .gs_gray")

# Vector for Dataframe creation
spcount <- 1
sppcount <- 1
sjcount <- 1
mpcount <- 1
mppcount <- 1
mjcount <- 1
stefanpapers <- rep("", 726)
stefanpartners <- rep("", 726)
stefanjournals <- rep("", 726)
moernerpapers <- rep("", 739)
moernerpartners <- rep("", 739)
moernerjournals <- rep("", 739)
for(i in 0:7){
  currsp <- html_nodes(get(paste0("stefandata",i)), ".gsc_a_at")%>%html_text()
  currmp <- html_nodes(get(paste0("moernerdata",i)), ".gsc_a_at")%>%html_text()
  currspp <- html_nodes(get(paste0("stefandata",i)), ".gs_gray")%>%html_text()
  currmpp <- html_nodes(get(paste0("moernerdata",i)), ".gs_gray")%>%html_text()
  for(i in currsp){
    stefanpapers[spcount] <- i
    spcount <- spcount + 1
  }
  for(i in currmp){
    moernerpapers[mpcount] <- i
    mpcount <- mpcount + 1
  }
  for(i in 1:length(currspp)){
    if(i%%2==1){
      stefanpartners[sppcount] <- currspp[i]
      sppcount <- sppcount + 1
    }else{
      stefanjournals[sjcount] <- currspp[i]
      sjcount <- sjcount + 1
    }
  }
  for(i in 1:length(currmpp)){
    if(i%%2==1){
      moernerpartners[mppcount] <- currmpp[i]
      mppcount <- mppcount + 1
    }else{
      moernerjournals[mjcount] <- currmpp[i]
      mjcount <- mjcount + 1
    }
  }
}

#Citations and Year Vector Creation
stefancitations <- html_table(stefandata0)[[2]][2][2:101,1]
stefanyear <- html_table(stefandata0)[[2]][3][2:101,1]
for(i in 1:7){
  tocollate <- html_table(get(paste0("stefandata", i)))[[2]][2]
  tocollate <- tocollate[2:length(tocollate[,1]), 1]
  stefancitations <-c(stefancitations, tocollate)
  yearcollate <- html_table(get(paste0("stefandata", i)))[[2]][3]
  yearcollate <- yearcollate[2:length(yearcollate[,1]), 1]
  stefanyear <-c(stefanyear, yearcollate) 
}
#Test for same length vectors
length(stefancitations)-length(stefanyear)


moernercitations <- html_table(moernerdata0)[[2]][2][2:101,1]
moerneryear <- html_table(moernerdata0)[[2]][3][2:101,1]
for(i in 1:7){
  tocollate <- html_table(get(paste0("moernerdata", i)))[[2]][2]
  tocollate <- tocollate[2:length(tocollate[,1]), 1]
  moernercitations <-c(moernercitations, tocollate)
  yearcollate <- html_table(get(paste0("moernerdata", i)))[[2]][3]
  yearcollate <- yearcollate[2:length(yearcollate[,1]), 1]
  moerneryear <-c(moerneryear, yearcollate) 
}
#Test for same length vectors
length(moernercitations)-length(moerneryear)

#Dataframe Creation
dfcolnames <- c("Papers", "Partners", "Journals", "Citations", "Years")
stefandf <- data.frame(stefanpapers, stefanpartners, stefanjournals, stefancitations, stefanyear)
colnames(stefandf) <- dfcolnames
head(stefandf)
moernerdf <- data.frame(moernerpapers, moernerpartners, moernerjournals, moernercitations, moerneryear)
colnames(moernerdf) <- dfcolnames
head(moernerdf)

# CSV Creation (Run once pls)
write.csv(stefandf, "../cleandata/stefan_hell_googlescholar.csv")
write.csv(moernerdf, "../cleandata/william_moerner_googlescholar.csv")
```
