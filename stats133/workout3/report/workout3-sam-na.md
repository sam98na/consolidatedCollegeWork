workout3-sam-na
================

``` r
library(dplyr)
```

    ## 
    ## Attaching package: 'dplyr'

    ## The following objects are masked from 'package:stats':
    ## 
    ##     filter, lag

    ## The following objects are masked from 'package:base':
    ## 
    ##     intersect, setdiff, setequal, union

``` r
library(stringr)
library(ggplot2)
library(wordcloud2)
library(webshot)
library(htmlwidgets)
library(reshape2)
```

``` r
#Importing CSV into Manipulatable Dataframes
stefandf <- read.csv("../data/cleandata/stefan_hell_googlescholar.csv")
moernerdf <- read.csv("../data/cleandata/william_moerner_googlescholar.csv")
```

-----

-----

# —Part 3—

-----

-----

# Number of Vowels

``` r
#Vowels
stefanvowels <- length(stefandf[grep("^[aeiouAEIOU]", stefandf$Papers),]$Papers)
moernervowels <- length(moernerdf[grep("^[aeiouAEIOU]", moernerdf$Papers),]$Papers)
print(paste("Stefan Hell has", stefanvowels, "papers that start with a vowel and William Moerner has", moernervowels, "papers that start with a vowel. Based on initial inspection, most of these papers in both researcher's lists are words that start with vowels, not just single vowel words (like 'a' or 'I', as I initially thought)"))
```

    ## [1] "Stefan Hell has 96 papers that start with a vowel and William Moerner has 168 papers that start with a vowel. Based on initial inspection, most of these papers in both researcher's lists are words that start with vowels, not just single vowel words (like 'a' or 'I', as I initially thought)"

-----

# Papers that End in S

``` r
#Ends with S
stefans <- length(stefandf[grep("[Ss]$", stefandf$Papers),]$Papers)
moerners <- length(moernerdf[grep("[Ss]$", moernerdf$Papers),]$Papers)
print(paste("Stefan Hell has", stefans, "papers that end with 's' and William Moerner has", moerners, "papers that end with an 's'. This implies that William Moerner works more with multiple objects at one time than Stefan Hell does(?)."))
```

    ## [1] "Stefan Hell has 180 papers that end with 's' and William Moerner has 270 papers that end with an 's'. This implies that William Moerner works more with multiple objects at one time than Stefan Hell does(?)."

-----

# Paper Lengths

``` r
stefandf$strlen <- str_count(stefandf$Papers)
moernerdf$strlen <- str_count(moernerdf$Papers)
maxstefan <- max(stefandf$strlen)
maxmoerner <- max(moernerdf$strlen)
print(paste("Stefan Hell longest paper name ranks in at", maxstefan, "characters and William Moerner comes in at", maxmoerner, "characters. Both are very close, meaning that either both have around the same conventions when naming papers or there is a soft cap of how long a paper name can really be."))
```

    ## [1] "Stefan Hell longest paper name ranks in at 177 characters and William Moerner comes in at 179 characters. Both are very close, meaning that either both have around the same conventions when naming papers or there is a soft cap of how long a paper name can really be."

-----

# Punctuation Frequency

``` r
#Punctuation Frequency
stefandf$punctcount <- str_count(stefandf$Papers, "[[:punct:]]")
ggplot(stefandf, aes(x = punctcount)) + geom_histogram()+ggtitle("Histogram of Stefan Hell's Punctuation Frequencies")+xlab("Punctuation Count")+ylab("Frequency")
```

    ## `stat_bin()` using `bins = 30`. Pick better value with `binwidth`.

![](workout3-sam-na_files/figure-gfm/unnamed-chunk-6-1.png)<!-- -->

``` r
moernerdf$punctcount <- str_count(moernerdf$Papers, "[[:punct:]]")

ggplot(moernerdf, aes(x = punctcount)) + geom_histogram()+ggtitle("Histogram of William Moerner's Punctuation Frequencies")+xlab("Punctuation Count")+ylab("Frequency")
```

    ## `stat_bin()` using `bins = 30`. Pick better value with `binwidth`.

![](workout3-sam-na_files/figure-gfm/unnamed-chunk-6-2.png)<!-- -->

``` r
summary(stefandf$punctcount)
```

    ##    Min. 1st Qu.  Median    Mean 3rd Qu.    Max. 
    ##   0.000   0.000   1.000   1.496   2.000  24.000

``` r
summary(moernerdf$punctcount)
```

    ##    Min. 1st Qu.  Median    Mean 3rd Qu.    Max. 
    ##   0.000   0.000   1.000   1.539   2.000  10.000

### Stefan Hell’s punctuation count tops out at a whopping 24 (looking at the specific title makes it seem that someone just included the names of the researchers in the title itself, leading to a bunch of commas and periods being tacked on to the total), whilst William Moerner’s punctuation count tops out at a more reasonable 10.

-----

# Removing Stop Words

``` r
#Remove Stop Words
stefandf$stripped <- str_replace_all(stefandf$Papers, "\\b((?i)a(?-i))\\b|\\b((?i)the(?-i))\\b|\\b((?i)an(?-i))\\b|\\b((?i)and(?-i))\\b|\\b((?i)in(?-i))\\b|\\b((?i)if(?-i))\\b|\\b((?i)but(?-i))\\b|[[:punct:]]|[0-9]", "")
moernerdf$stripped <- str_replace_all(moernerdf$Papers, "\\b((?i)a(?-i))\\b|\\b((?i)the(?-i))\\b|\\b((?i)an(?-i))\\b|\\b((?i)and(?-i))\\b|\\b((?i)in(?-i))\\b|\\b((?i)if(?-i))\\b|\\b((?i)but(?-i))\\b|[[:punct:]]|[0-9]", "")
head(stefandf, 2)
```

    ##   X
    ## 1 1
    ## 2 2
    ##                                                                                                                    Papers
    ## 1 Breaking the diffraction resolution limit by stimulated emission: stimulated-emission-depletion fluorescence microscopy
    ## 2                                                                                             Far-field optical nanoscopy
    ##              Partners                              Journals Citations
    ## 1 SW Hell, J Wichmann Optics letters 19 (11), 780-782, 1994      4610
    ## 2             SW Hell   science 316 (5828), 1153-1158, 2007      2785
    ##   Years strlen punctcount
    ## 1  1994    119          3
    ## 2  2007     27          1
    ##                                                                                                            stripped
    ## 1 Breaking  diffraction resolution limit by stimulated emission stimulatedemissiondepletion fluorescence microscopy
    ## 2                                                                                        Farfield optical nanoscopy

-----

# Top 10 Words

``` r
#Top 10 Words for Each Researcher
listowords <- c()
for(i in stefandf$stripped){
  split <- strsplit(i, " ")
  listowords <- c(listowords, split[[1]])
}
listowords <- listowords[!listowords == ""]
stefanwordfreq <- as.data.frame(table(listowords))
head(stefanwordfreq[order(stefanwordfreq$Freq, decreasing = TRUE),], 10)
```

    ##        listowords Freq
    ## 1324           of  343
    ## 1167   microscopy  180
    ## 2198         with  166
    ## 683  fluorescence  110
    ## 1889         STED   97
    ## 720           for   92
    ## 1263    nanoscopy   86
    ## 222            by   72
    ## 1671   resolution   72
    ## 883       imaging   67

``` r
listowordm <- c()
for(i in moernerdf$stripped){
  split <- strsplit(i, " ")
  listowordm <- c(listowordm, split[[1]])
}
listowordm <- listowordm[!listowordm == ""]
moernerwordfreq <- as.data.frame(table(listowordm))
head(moernerwordfreq[order(moernerwordfreq$Freq, decreasing = TRUE),], 10)
```

    ##           listowordm Freq
    ## 1251              of  403
    ## 694              for  118
    ## 1720          single  107
    ## 1387 photorefractive   73
    ## 1728  singlemolecule   73
    ## 860          imaging   68
    ## 2099            with   65
    ## 1126       molecules   64
    ## 1789    spectroscopy   61
    ## 213               by   55

``` r
print("Surprisingly, 'of' is both Stefen's and William's most used word (surprising in that they both have the same most used word and that the word 'of' is not considered a stop word), followed by terms that seem to be their area of expertise, like 'STED' and 'photorefractive'.")
```

    ## [1] "Surprisingly, 'of' is both Stefen's and William's most used word (surprising in that they both have the same most used word and that the word 'of' is not considered a stop word), followed by terms that seem to be their area of expertise, like 'STED' and 'photorefractive'."

-----

-----

# —Part 4—

-----

-----

# Wordclouds

``` r
###CHANGE EVAL TO TRUE BEFORE SUBMITTING PLS###


#WordCloud
webshot::install_phantomjs()
stefanwc <- wordcloud2(stefanwordfreq)
saveWidget(stefanwc, "stef.html", selfcontained=F)
webshot::webshot("stef.html","../images/stefan_hell_wordcloud.png",vwidth = 1400, vheight = 800, delay =25)
moernerwc <- wordcloud2(moernerwordfreq)
saveWidget(moernerwc, "moer.html", selfcontained=F)
webshot::webshot("moer.html","../images/william_moerner_wordcloud.png",vwidth = 1400, vheight = 800, delay =25)
```

<img src="../images/stefan_hell_wordcloud.png" style="display: block; margin: auto;" /><img src="../images/william_moerner_wordcloud.png" style="display: block; margin: auto;" />

### Based on the wordclouds, both have “of” as their most used word, but the disparity between Stefan and William in their second most used words seems to be wide (Stefan’s words have a more gradual decline in size from “of” to words like “microscopy”, whilst William second largest word “photorefractive” is much smaller in comparison).

-----

# Publications Per Year

``` r
#Line Plot for Publications/Year
stefyears <- as.data.frame(table(stefandf$Years))
moerneryears <- as.data.frame(table(moernerdf$Years))

png("../images/stefan_hell_publicationsperyear.png")
ggplot(stefyears, aes(Var1, Freq, group = 1))+geom_line()+xlab("Years")+ylab("Frequencies")+ggtitle("Stefan Hell's Publications Per Year")+theme(axis.text.x = element_text(angle = 90))
dev.off()
```

    ## png 
    ##   2

``` r
png("../images/william_moerner_publicationsperyear.png")
ggplot(moerneryears, aes(Var1, Freq, group = 1))+geom_line()+xlab("Years")+ylab("Frequencies")+ggtitle("William Moerner's Publications Per Year")+theme(axis.text.x = element_text(angle = 90))
dev.off()
```

    ## png 
    ##   2

<img src="../images/stefan_hell_publicationsperyear.png" style="display: block; margin: auto;" /><img src="../images/william_moerner_publicationsperyear.png" style="display: block; margin: auto;" />

### We can see that whilst Stefan Hell achieved the highest amount of publications published in a year (around 2010), William Moerner maintained a steady pace of around 30-35 publications a year in a time period where Stefan Hell was only sporadically reaching 20 publications (in the early 1980’s).

-----

# Top Three Words

``` r
#Top Three Words
stefanthree <- c("microscopy", "with", "STED")
moernerthree <- c("single", "imaging", "molecules")
stefandf$microscopy <- as.numeric(str_detect(stefandf$Papers, "\\b((?i)microscopy(?-i))\\b"))
stefandf$with <- as.numeric(str_detect(stefandf$Papers, "\\b((?i)with(?-i))\\b"))
stefandf$STED <- as.numeric(str_detect(stefandf$Papers, "\\b((?i)STED(?-i))\\b"))
moernerdf$single <- as.numeric(str_detect(moernerdf$Papers, "\\b((?i)single(?-i))\\b"))
moernerdf$imaging <- as.numeric(str_detect(moernerdf$Papers, "\\b((?i)imaging(?-i))\\b"))
moernerdf$molecules <- as.numeric(str_detect(moernerdf$Papers, "\\b((?i)molecules(?-i))\\b"))


stefanyearswords <- data.frame(stefandf$Years, stefandf$microscopy, stefandf$with, stefandf$STED)
moerneryearswords <- data.frame(moernerdf$Years, moernerdf$single, moernerdf$imaging, moernerdf$molecules)


stefantopthree <- aggregate(stefanyearswords, list(stefanyearswords$stefandf.Years), sum)
stefantopthree <- subset(stefantopthree, select = -c(stefandf.Years))
stefantopthree <- stefantopthree[-c(1),]
moernertopthree <- aggregate(moerneryearswords, list(moerneryearswords$moernerdf.Years), sum)
moernertopthree <- subset(moernertopthree, select = -c(moernerdf.Years))

stefantopthree <- melt(stefantopthree, "Group.1")
moernertopthree <- melt(moernertopthree, "Group.1")

png("../images/stefanhelltopthreewords.png")
ggplot(stefantopthree, aes(x = Group.1))+geom_line(aes(y = value, color = variable))+xlab("Years")+ylab("Frequency")+ggtitle("Stefan Hell's Top Three Word Frequencies over the Years")
dev.off()
```

    ## png 
    ##   2

``` r
png("../images/williammoernertopthreewords.png")
ggplot(moernertopthree,aes(x = Group.1))+geom_line(aes(y = value, color = variable))+xlab("Years")+ylab("Frequency")+ggtitle("William Moerner's Top Three Word Frequencies over the Years")
dev.off()
```

    ## png 
    ##   2

<img src="../images/stefanhelltopthreewords.png" style="display: block; margin: auto;" /><img src="../images/williammoernertopthreewords.png" style="display: block; margin: auto;" />

### We can see that Stefan’s top three words are used in a more distributed manner over the years, whilst William Moerner’s takes off in the 2000’s, not really using those words before then.

-----

-----

# —Part 5—

-----

-----

# Q1

``` r
stefandf$commacount <- stefandf$Partners %>% str_count(",")
moernerdf$commacount <- moernerdf$Partners%>%str_count(",")
stefanavgpartners <- mean(stefandf$commacount)
moerneravgpartners <- mean(moernerdf$commacount)
print(paste0("Stefan Average Partner Count = ", stefanavgpartners, "| William Average Partner Count = ", moerneravgpartners))
```

    ## [1] "Stefan Average Partner Count = 3.5633608815427| William Average Partner Count = 3.1041948579161"

### On average (using a slightly flawed but usable metric of taking one comma to mean one partner in each Partner dataframe column), both researchers have around 3 partners, with Stefan edging out William with a 0.4 partner advantage. Given such a small difference between the two, we can assume that they both have similar tolerances/reliances on how many partners they have.

-----

# Q3

-----

``` r
stefandf$moerner <- stefandf$Partners %>% str_detect("\\b((?i)moerner(?-i))\\b")
moernerdf$stefan <- moernerdf$Partners%>%str_detect("\\b((?i)hell(?-i))\\b")
stefanmoerner <- stefandf%>%filter(moerner==TRUE)
moernerstefan <- moernerdf%>%filter(stefan==TRUE)

stefanmoerner$Papers
```

    ## [1] The nobel prize in chemistry 2014                                
    ## [2] How the optical microscope became a nanoscope                    
    ## [3] Nobel Prize in Chemistry: Fascinating glimpses into the nanoworld
    ## [4] Nobel Prizes 2014                                                
    ## [5] A mi rosz ópos feloldási orlát áttörése                          
    ## [6] Single Molecule Approaches to Biology                            
    ## 683 Levels: [8] Bioenergetic characterization of <U+03B3>-aminobutyric acid transporter of synaptic vesicles ...

``` r
moernerstefan$Papers
```

    ## [1] The nobel prize in chemistry 2014                                         
    ## [2] How the optical microscope became a nanoscope                             
    ## [3] EMBO/EMBL Symposium-Seeing is Believing: Submit your Abstract by 10th July
    ## [4] Nobelpreis für Chemie: Faszinierende Blicke in die Nanowelt               
    ## [5] Nobel Prize in Chemistry: Fascinating glimpses into the nanoworld         
    ## [6] A mikroszkópos feloldási korlát áttörése                                  
    ## [7] Committees and sponsors                                                   
    ## [8] Single Molecule Approaches to Biology                                     
    ## 727 Levels: " Frequency Domain Optical Storage and Other Applications of Persistent Spectral Hole-Burning" Persistent Spectral Hole-Burning ...

### Stefan Hell and William Moerner have worked on a couple of papers together, including mainly ones for winning the Nobel Prize for Chemistry in 2014, optical microscopes and how they became a nanoscope, and single molecule approaches to biology. On first glance, it may seem as though William Moerner has worked on more papers with Stefan Hell than Hell has worked on with Moerner (which would be impossible), but the two extra papers that Moerner has seems to be a German version of another paper that they worked on and a logistical paper about abstract submitting, which aren’t relevant.

-----

# Q2

-----

### Using the overlap tables created in Q3, we can see that they have a few mutual friends, such as NH Dekker, M Sauer, and K Welter, which they have both collaborated on papers together. One person of special note is E Betzig, full name Eric Betzig, who won the Nobel Prize for Chemistry alongside both Stefan and William in 2014.

-----

# Q4

-----

``` r
head(stefandf[order(stefandf$commacount, decreasing = TRUE),], 2)
```

    ##       X
    ## 531 531
    ## 11   11
    ##                                                                                    Papers
    ## 531 POSTER SESSION 2-ON BEHALF OF THE WORKING GROUP ON CARDIAC CELLULAR ELECTROPHYSIOLOGY
    ## 11            Fluorescence nanoscopy by ground-state depletion and single-molecule return
    ##                                                                        Partners
    ## 531 M Liu, GB Shi, H Liu, SC Dudley, S Burel, F Coyan, MR Meyer, CF Litchi, ...
    ## 11          J Fölling, M Bossi, H Bock, R Medda, CA Wurm, B Hein, S Jakobs, ...
    ##                                          Journals Citations Years strlen
    ## 531 EP Europace 17 (suppl_3), iii132-iii135, 2015            2015     85
    ## 11               Nature methods 5 (11), 943, 2008       715  2008     75
    ##     punctcount
    ## 531          1
    ## 11           2
    ##                                                                             stripped
    ## 531 POSTER SESSION ON BEHALF OF  WORKING GROUP ON CARDIAC CELLULAR ELECTROPHYSIOLOGY
    ## 11            Fluorescence nanoscopy by groundstate depletion  singlemolecule return
    ##     microscopy with STED commacount moerner
    ## 531          0    0    0          8   FALSE
    ## 11           0    0    0          7   FALSE

``` r
head(moernerdf[order(moernerdf$commacount, decreasing = TRUE),], 2)
```

    ##     X
    ## 31 31
    ## 50 50
    ##                                                                                                                Papers
    ## 31                                                   A spindle-like apparatus guides bacterial chromosome segregation
    ## 50 Superresolution imaging of targeted proteins in fixed and living cells using photoactivatable organic fluorophores
    ##                                                                       Partners
    ## 31 JL Ptacin, SF Lee, EC Garner, E Toro, M Eckart, LR Comolli, WE Moerner, ...
    ## 50         HD Lee, SJ Lord, S Iwanaga, K Zhan, H Xie, JC Williams, H Wang, ...
    ##                                                                Journals
    ## 31                                Nature cell biology 12 (8), 791, 2010
    ## 50 Journal of the American Chemical Society 132 (43), 15099-15101, 2010
    ##    Citations Years strlen punctcount
    ## 31       298  2010     64          1
    ## 50       157  2010    114          0
    ##                                                                                                         stripped
    ## 31                                                 spindlelike apparatus guides bacterial chromosome segregation
    ## 50 Superresolution imaging of targeted proteins  fixed  living cells using photoactivatable organic fluorophores
    ##    single imaging molecules commacount stefan
    ## 31      0       0         0          7  FALSE
    ## 50      0       1         0          7  FALSE

### The paper with the most co-authors between these two researchers is a paper called “POSTER SESSION 2-ON BEHALF OF THE WORKING GROUP ON CARDIAC CELLULAR ELECTROPHYSIOLOGY” on Stefan Hell’s article list. Based on the abstract given online, it deals with the activation of unfolded protein response (UPR), and how it might affect other cardiac ion channels other than the ones in the endoplasmic reticulum (all of which sounds extremely complicated and would befit a whopping 8 co-authors).

-----

# Q5

-----

``` r
stefandf$pages <- str_match(stefandf$Journals, ", ([0-9|-]*?),")
moernerdf$pages <- str_match(moernerdf$Journals, ", ([0-9|-]*?),")
```

-----

# Q9

-----

``` r
#colnames(stefandf)
#head(stefandf[order(stefandf$Citations, decreasing=TRUE),])
stefandf[stefandf$Citations == max(as.numeric(stefandf$Citations)),]
```

    ##     X
    ## 60 60
    ##                                                                   Papers
    ## 60 Red-Emitting Rhodamine Dyes for Fluorescence Microscopy and Nanoscopy
    ##                                                                     Partners
    ## 60 K Kolmakov, VN Belov, J Bierwagen, C Ringemann, V Müller, C Eggeling, ...
    ##                                              Journals Citations Years
    ## 60 Chemistry-A European Journal 16 (1), 158-166, 2010       210  2010
    ##    strlen punctcount
    ## 60     69          1
    ##                                                             stripped
    ## 60 RedEmitting Rhodamine Dyes for Fluorescence Microscopy  Nanoscopy
    ##    microscopy with STED commacount moerner    pages.1 pages.2
    ## 60          1    0    0          6   FALSE , 158-166, 158-166

``` r
moernerdf[moernerdf$Citations == max(as.numeric(moernerdf$Citations)),]
```

    ##     X
    ## 47 47
    ##                                                                                                              Papers
    ## 47 ADP-induced rocking of the kinesin motor domain revealed by single-molecule fluorescence polarization microscopy
    ##                                           Partners
    ## 47 H Sosa, EJG Peterman, WE Moerner, LSB Goldstein
    ##                                                  Journals Citations Years
    ## 47 Nature Structural & Molecular Biology 8 (6), 540, 2001       169  2001
    ##    strlen punctcount
    ## 47    112          2
    ##                                                                                                       stripped
    ## 47 ADPinduced rocking of  kinesin motor domain revealed by singlemolecule fluorescence polarization microscopy
    ##    single imaging molecules commacount stefan pages.1 pages.2
    ## 47      1       0         0          3  FALSE  , 540,     540
