wfile<-"ratio.correlations.w"
nwfile<-"ratio.correlations.nw"
dw<-read.table(wfile)
dnw<-read.table(nwfile)

plotcorr<- function(d) {
    maxs<-c(max(d$V2,na.rm=TRUE),max(d$V3,na.rm=TRUE))

    bio1<-d$V2[d$V4=="bio"]
    bio2<-d$V3[d$V4=="bio"]
    xtal1<-d$V2[d$V4=="xtal"]
    xtal2<-d$V3[d$V4=="xtal"]
    gray1<-d$V2[d$V4=="gray"]
    gray2<-d$V3[d$V4=="gray"]
    
    plot(bio1,bio2,xlim=c(0,maxs[1]),ylim=c(0,maxs[2]),col=1,xlab="score 1st",ylab="score 2nd",main="entropy scores 1st vs 2nd interface partners")
    points(xtal1,xtal2,col=2,pch=4)
    points(gray1,gray2,col=3,pch=1)
    abline(h=0.95,lty=2,col=8)
    abline(h=1.05,lty=2,col=8)
    abline(v=0.95,lty=2,col=8)
    abline(v=1.05,lty=2,col=8)
}
plotcorr(dnw)
