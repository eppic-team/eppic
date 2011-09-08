# distribution of interface areas from a set of monomers (1 chain biounits as annotated in PDB)
d<-read.table("/afs/psi.ch/project/bioinfo2/kaks/monomer_interfaces/monomer_interfaces.areas")
hist(d$V4,100,main="Interface area distribution for PDB-annotated monomers",xlab=expression(paste("interface area (", A^2, ")", sep = "")))
abline(v=800,col=2)

s<-summary(d$V4)
legend(x=1500,y=400,names(s),bty="n")
legend(x=2100,y=400,s,bty="n")

# for a high resolution png, use: (note with the png function it doesn't work, it has to be bitmap)
bitmap("distribution.png",type="png256",res=1200)