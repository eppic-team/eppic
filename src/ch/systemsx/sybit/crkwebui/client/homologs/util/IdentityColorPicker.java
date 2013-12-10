package ch.systemsx.sybit.crkwebui.client.homologs.util;

public class IdentityColorPicker {
	
	public static String getColor(double fraction){
		long[] rgb = {1,1,1};
		
		if(fraction >= 0.0 && fraction <= 0.1){
			rgb[0]=165; rgb[1]=0; rgb[2]=38;
		} else if(fraction > 0.1 && fraction <= 0.2){
			rgb[0]=215; rgb[1]=48; rgb[2]=39;
		} else if(fraction > 0.2 && fraction <= 0.3){
			rgb[0]=244; rgb[1]=109; rgb[2]=67;
		} else if(fraction > 0.3 && fraction <= 0.4){
			rgb[0]=253; rgb[1]=174; rgb[2]=97;
		} else if(fraction > 0.4 && fraction <= 0.5){
			rgb[0]=254; rgb[1]=224; rgb[2]=139;
		}else if(fraction > 0.5 && fraction <= 0.6){
			rgb[0]=217; rgb[1]=239; rgb[2]=139;
		} else if(fraction > 0.6 && fraction <= 0.7){
			rgb[0]=166; rgb[1]=217; rgb[2]=106;
		} else if(fraction > 0.7 && fraction <= 0.8){
			rgb[0]=102; rgb[1]=189; rgb[2]=99;
		} else if(fraction > 0.8 && fraction <= 0.9){
			rgb[0]=26; rgb[1]=152; rgb[2]=80;
		} else if(fraction > 0.9 && fraction <= 1.0){
			rgb[0]=0; rgb[1]=104; rgb[2]=55;
		}
		
		return "rgb("+rgb[0]+","+rgb[1]+","+rgb[2]+")";
	}

}
