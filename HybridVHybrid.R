source1 <- read.delim(file.choose())
Current_Hybrid <- read.delim("~/University/BSc/Wildfire-Fork/hybrid.txt")
Astar <- read.delim("~/University/BSc/Wildfire-Fork/Astar.txt")

head(Current_Hybrid)
head(source1)
s_mean <- mean(source1[0:nrow(Current_Hybrid),1]) 
s_mean
s_mean <- mean(source1[500:999,1]) #Only take lines 500-999


c_mean <- mean(Current_Hybrid[0:nrow(Current_Hybrid),1])
a_mean <- mean(Astar[,1])
a_mean
c_mean
s_mean
if(s_mean < c_mean){
  print("Current implementation is worse")
}else{
  print("Current implementation is better")
}
t.test(source1[1:nrow(Current_Hybrid),1], Current_Hybrid[1:nrow(Current_Hybrid),1], alternative = "two.sided")

if(c_mean < a_mean){
  print("HOT DAMN! Better than A*")
}else{
  print("Worse than A*")
}
t.test(source1[,1],Astar[,1], paired = TRUE, alternative = "two.sided")

