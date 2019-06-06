
if(TRUE){
Astar <- read.delim("~/University/BSc/Wildfire-Fork/Astar.txt", header=FALSE)
Hybrid <- read.delim("~/University/BSc/Wildfire-Fork/hybrid.txt", header=FALSE)
nrow(Astar)
nrow(Hybrid)
}
Hybrid
#Astar
#Hybrid

if(TRUE){
if(nrow(Hybrid) < nrow(Astar)){
  gen<- seq(1,nrow(Hybrid), 1)
}else{
  gen<- seq(1,nrow(Astar), 1)
}

delta <- list()
for(i in 1: length(gen))
   delta[i] <-Hybrid[i,1] - Astar[i,1]

hgen <- seq(1,nrow(Hybrid), 1)
agen <- seq(1,nrow(Astar), 1)

}

     

plot(hgen, Hybrid[,1], log = "" )
plot(agen, Astar[,1], log = "")

if(TRUE){
  if(length(hgen) > length(agen)){
    plot(hgen, Hybrid[,1], log ="", col="#ff9999", ylim = c(0,1))
    points(agen, Astar[,1], col="#33ffff")
  }else{
    plot(agen, Astar[,1], log ="", col="#33ffff", ylim = c(0,1))
    points(hgen, Hybrid[,1], col="#ff9999")
  }
red = smooth.spline(hgen, Hybrid[,1], spar=0.8)
blue = smooth.spline(agen, Astar[,1], spar=1.2)
lines(red, col="red")
lines(blue, col="blue")
}

if(TRUE){
plot(gen, delta, log= "", col="grey")
smoothingSpline = smooth.spline(gen, delta, spar=0.8)
lines(smoothingSpline)
abline(h=0)
}
#library(ggplot2)
#qplot(as.numeric(gen),as.numeric(delta)) + geom_smooth(span=1000)
#geom_segment(as.numeric(gen), as.numeric(delta))
