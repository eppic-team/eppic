args<-commandArgs(trailingOnly=TRUE)
file<-args[1]
out<-args[2]
ymax<-as.numeric(args[3])

d<-read.table(file)


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

plotit <- function(d, sizes, ymax) {

	ncols=1
	layout(mat=matrix(1:length(sizes), length(sizes), ncols, byrow=FALSE)) # rows, columns
	
	for (size in sizes) {		
		a<-getAvrgdVals(d,size)
		plot(d$V2,a,xlab="residue",ylab="evolutionary score",type="l",ylim=c(0,ymax))
	}

}

postscript(out,horizontal=FALSE)
plotit(d,c(4,20,40,50),ymax)
dev.off()
