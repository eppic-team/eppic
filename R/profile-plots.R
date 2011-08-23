args<-commandArgs(trailingOnly=TRUE)
entfile<-args[1]
kaksfile<-args[2]
out<-args[3]
#ymax<-as.numeric(args[4])

dent<-read.table(entfile)
dkaks<-read.table(kaksfile)


# a cheap derivative function 
myder<- function(x,y,d) {
	der<-c()
	for (i in seq(1,length(y))) {
		der<-c(der,(y[i+d]-y[i])/d)
	}
	der
}

getAvrgdVals <- function(d, size) {

	vals<-d$V3

	halfwindow<-round(size/2)

	avrg<-c()
	for (i in seq(1,length(vals))) {
		start=i-halfwindow
		end=i+halfwindow
		if (start<0) {
			start=0
		}
		if (end>length(vals)) {
			end=length(vals)
		}
		avrg<-c(avrg,mean(vals[start:end]))
	}
	avrg
}

plotit <- function(dent, dkaks, main, sizes) {

	ncols=1
	par(mfrow=c(length(sizes),ncols))
	#layout(mat=matrix(1:length(sizes), length(sizes), ncols, byrow=FALSE)) # rows, columns
	
	for (size in sizes) {		
		aent<-getAvrgdVals(dent,size)
		akaks<-getAvrgdVals(dkaks,size)
		ymax=max(aent,akaks)
		plot(dent$V2,aent,xlab="residue",ylab="evolutionary score",type="l",ylim=c(0,ymax),main=paste(main," (",size," window size)",sep=""))
		points(dkaks$V2,akaks,type="l",ylim=c(0,ymax),col=4)
		cor<-cor(aent,akaks)
		#plot(aent,akaks)
		legend(x=max(dent$V2)-max(dent$V2)/10,y=ymax-ymax/10,sprintf("%3.2f",cor),bty="n")
	}

}

# ratios of evol scores of random samples of size size, for kaks and entropies. We plot the correlation of the ratios 
ratiosamples <- function(dent,dkaks,size,n) {

	ratios.ent<-c()
	ratios.kak<-c()
	for (i in seq(1,n)) {
		num.samp<-sample(dent$V2,size)
		den.samp<-sample(dent$V2,size)
		num.ent<-dent$V3[num.samp]
		den.ent<-dent$V3[den.samp]
		num.kak<-dkaks$V3[num.samp]
		den.kak<-dkaks$V3[den.samp]
		
		if (mean(den.ent)>0.1 & mean(den.kak)>0.1 & mean(num.ent)>0.1 & mean(den.kak)>0.1) {
			ratio.ent<-mean(num.ent)/mean(den.ent)
			ratio.kak<-mean(num.kak)/mean(den.kak)
				
			ratios.ent<-c(ratios.ent,ratio.ent)
			ratios.kak<-c(ratios.kak,ratio.kak)
		}
	}
	cor<-cor(ratios.ent,ratios.kak)
	par(mfrow=c(1,2))
	plot(ratios.ent,ratios.kak)
	ymax<-max(ratios.kak)
	legend(0.5,ymax-ymax/10,sprintf("%3.2f",cor),bty="n")
}

main=strsplit(basename(kaksfile),".kaks")[[1]]
postscript(out,horizontal=TRUE)
plotit(dent,dkaks,main,c(1,4))
ratiosamples(dent,dkaks,6,1000)
dev.off()
