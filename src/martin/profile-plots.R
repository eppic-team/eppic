args<-commandArgs(trailingOnly=TRUE)
uniprot<-args[1]
ymax<-as.numeric(args[2])
xstraight<-as.numeric(unlist(strsplit(args[3],',')))

data<-read.table(paste("values_",uniprot,".txt",sep=""), comment.char="#", col.names=c("Nr","Aa","Entropy","KaKs","SecPred","SS","Conf","SignalP"))
valuesss=rep.int(x=ymax/2,times=length(data$SS))
cols <- as.character(cut(data$SS,breaks = c(0,1,2,3),labels=c("#FF000090","#00FF0060","#FFB90F50")))
valuesss<-valuesss*(data$Conf+0.1)
cysteinx<-data$Nr[data$Aa=="C"]
cysteins<-cbind(cysteinx,rep.int(x=0,times=length(cysteinx)))


signalp=rep.int(x=-ymax*.05,times=length(data$Nr[data$SignalP=="1"]))
# color ss-elements of signal peptide grey
if (length(signalp) > 0){
	for (i in seq(1,length(signalp))) {
		cols[i] <- "#8C8C8C"
	}
}


# a cheap derivative function 
myder<- function(x,y,d) {
	der<-c()
	for (i in seq(1,length(y))) {
		der<-c(der,(y[i+d]-y[i])/d)
	}
	der
}

getAvrgdVals <- function(vals, size) {

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

plotit <- function(data, sizes, ymax) {

	ncols=1
	par(mfrow=c(length(sizes),1),xpd=NA)

	for (size in sizes) {		
		a<-getAvrgdVals(data$Entropy,size)
		b<-getAvrgdVals(data$KaKs,size)
		barplot(valuesss,col=cols,border=NA,space=0,ylim=c(0,ymax),xlab="residue",ylab="evolutionary score",main=paste("Window size:", size))

		# display signal peptide as ticks, unused
		#barplot(signalp,col="#8C8C8C",border=NA,space=0,ylim=c(-ymax*0.1,0),add=TRUE)
		axis(1,at=seq(0,max(data$Nr),50),tick=TRUE)
		axis(1,at=seq(0,max(data$Nr),10),tick=TRUE,lwd.ticks=2, labels=FALSE)
		points(data$Nr,a,xlab="residue",ylab="evolutionary score",type="l",ylim=c(0,ymax),main=size,col=1)
		lines(data$Nr,b,xlab="residue",ylab="evolutionary score",type="l",ylim=c(0,ymax),main=size,col=4,lwd=2)
		points(cysteins,type="p",col="#008B00",pch=17)
		axis(1,at=cysteinx,col.ticks="#008B00",labels=paste("C",cysteinx,sep=""),cex.axis=0.5,col.axis="#008B00",mgp=c(3,0.2,0))
		if (exists("xstraight")){
			ystraight<-rep.int(x=0,times=length(xstraight))
			segments(xstraight, ystraight,xstraight,ystraight+(ymax*0.75),col=6)
			text(xstraight,ystraight+(ymax*0.75), xstraight, col=6, adj=c(-.1,-.1),srt=60,cex=0.6)
		}
	}
		legend(0,ymax+(0.38*ymax),c("Entropy","KaKs"),cex=0.8,col=c(1,4),lty=1,lwd=4,merge=TRUE,bty="n",hor=TRUE)
		legend(0,ymax+(0.30*ymax),c(expression(paste(alpha,"-helix",sep="")),expression(paste(beta,"-strand",sep="")),"Coil","SignalPeptide"),cex=0.8,col=c("#FF000090","#00FF0060","#FFB90F50","#8C8C8C"),lty=1,lwd=4,merge=TRUE,bty="n",hor=TRUE)
		legend(0,ymax+(0.22*ymax),c(paste("Cys", toString(cysteinx))),cex=0.8,col=c("#008B00"),lty=0,lwd=4,merge=TRUE,bty="n",pch=17,hor=TRUE)
}

pdf(paste(uniprot,"_boundary.pdf",sep=""), onefile=TRUE,version="1.4",width=15,height=9)
plotit(data,c(10,15),ymax)
plotit(data,c(20,25),ymax)
plotit(data,c(30,35),ymax)
plotit(data,c(40,45),ymax)
dev.off()


