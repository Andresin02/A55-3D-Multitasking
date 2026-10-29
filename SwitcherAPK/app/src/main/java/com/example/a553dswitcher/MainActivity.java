package com.example.a553dswitcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final String PREFS="a55_ultra_preferences";
    private static final String P_PALETTE="palette_index", P_TEXTURE="texture_index";
    private final Handler main=new Handler(Looper.getMainLooper());
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private int generation=0, paletteIndex=1, textureIndex=0;
    private SharedPreferences prefs;
    private PackageManager pm;
    private LinearLayout root,cardsRow;
    private TextView countLabel;
    private static final class Palette {
        String name,desc; int bg,mid,card,accent,glow,ink,muted;
        Palette(String n,String d,int b,int m,int c,int a,int g,int i,int u){name=n;desc=d;bg=b;mid=m;card=c;accent=a;glow=g;ink=i;muted=u;}
    }
    private final Palette[] palettes={
        new Palette("Obsidian","Negro y plata",0xff080b12,0xff171e2b,0xff202a38,0xffb9d9f5,0xff66cfff,0xfff0f6ff,0xffa7b4c7),
        new Palette("Liquid Glass","Cristal azul y cian",0xff07131d,0xff102a39,0xff17384a,0xffa6f2ff,0xff28d9ff,0xffeffcff,0xffa7c5d4),
        new Palette("Aurora","Verde y turquesa",0xff071610,0xff10291f,0xff19392f,0xffb2ffe1,0xff47f5b0,0xffedfff7,0xffa5c7b8),
        new Palette("Cyberpunk","Magenta y violeta",0xff13091d,0xff281333,0xff382044,0xffffc4f5,0xfff04cff,0xfffff1ff,0xffd1b1d5),
        new Palette("Royal Purple","Púrpura y lavanda",0xff100b20,0xff211735,0xff30254c,0xffe0d0ff,0xffa783ff,0xfff7f1ff,0xffc0b3d9),
        new Palette("Arctic","Hielo y azul",0xff07111c,0xff14273a,0xff24394d,0xffe2f5ff,0xff65afff,0xfff5fbff,0xffb1c4d8),
        new Palette("Rose Gold","Rosa y cobre",0xff1b1015,0xff322029,0xff49303a,0xffffd8cf,0xffff9dad,0xfffff5f1,0xffd3b4ba),
        new Palette("Emerald","Esmeralda y jade",0xff061711,0xff0d2d22,0xff174638,0xffd0ffe8,0xff38e5a4,0xffedfff7,0xffa5cdbb),
        new Palette("Solar Gold","Ámbar y oro",0xff191107,0xff30220d,0xff45331a,0xffffe6a6,0xffffc34d,0xfffff8e8,0xffd0bd91),
        new Palette("Crimson","Rubí y rojo oscuro",0xff18090d,0xff30121a,0xff48202b,0xffffd4dc,0xffff526f,0xfffff1f3,0xffd5aab4),
        new Palette("Pearl","Perla y plata",0xff151a21,0xff252d38,0xffdce5ef,0xffffffff,0xff9edbff,0xff17212d,0xff526274),
        new Palette("Ocean","Marino y aqua",0xff06111c,0xff0d2639,0xff16334b,0xffb4f4ff,0xff22bceb,0xffeffbff,0xffa4c4d7)
    };
    private final String[] textures={"Liquid Glass","Cristal facetado","Metal cepillado","Obsidiana","Cuero","Piedra","Papel","Agua"};
    private Palette p(){return palettes[paletteIndex];}
    private int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+0.5f);}
    private TextView label(String s,int size,int color,boolean bold){
        TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.CENTER_VERTICAL);
        if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;
    }
    private Drawable material(int color,int accent,int radius){return new MaterialDrawable(color,accent,dp(radius),textures[textureIndex],getResources().getDisplayMetrics().density);}
    @Override public void onCreate(Bundle b){
        super.onCreate(b);pm=getPackageManager();prefs=getSharedPreferences(PREFS,MODE_PRIVATE);
        paletteIndex=clamp(prefs.getInt(P_PALETTE,1),0,palettes.length-1);
        textureIndex=clamp(prefs.getInt(P_TEXTURE,0),0,textures.length-1);
        getWindow().setStatusBarColor(p().bg);getWindow().setNavigationBarColor(p().bg);
        buildShell();refreshApps();
    }
    private int clamp(int n,int lo,int hi){return Math.max(lo,Math.min(hi,n));}
    private void buildShell(){
        Palette a=p();root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(18),dp(14),dp(18),dp(10));
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{a.bg,a.mid,a.bg}));
        root.addView(label("A55 • 3D MULTITASKING ULTRA",11,a.glow,true),new LinearLayout.LayoutParams(-1,dp(24)));
        root.addView(label("Recent apps",29,a.ink,true),new LinearLayout.LayoutParams(-1,dp(40)));
        TextView pb=label("◈  PALETA: "+a.name+"  ▾",13,a.accent,true);pb.setGravity(Gravity.CENTER);pb.setBackground(material(a.card,a.glow,17));
        pb.setOnClickListener(v->choosePalette());LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(42));bp.setMargins(0,dp(5),0,dp(5));root.addView(pb,bp);
        TextView tb=label("▧  TEXTURA: "+textures[textureIndex]+"  ▾",13,a.accent,true);tb.setGravity(Gravity.CENTER);tb.setBackground(material(a.card,a.accent,17));
        tb.setOnClickListener(v->chooseTexture());LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,dp(42));tp.setMargins(0,0,0,dp(5));root.addView(tb,tp);
        countLabel=label("Preparando aplicaciones…",12,a.muted,false);root.addView(countLabel,new LinearLayout.LayoutParams(-1,dp(26)));
        HorizontalScrollView scroll=new HorizontalScrollView(this);scroll.setHorizontalScrollBarEnabled(false);scroll.setClipToPadding(false);scroll.setPadding(dp(2),dp(10),dp(2),dp(10));
        cardsRow=new LinearLayout(this);cardsRow.setOrientation(LinearLayout.HORIZONTAL);scroll.addView(cardsRow);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        TextView refresh=label("↻   Refresh apps",14,a.accent,true);refresh.setGravity(Gravity.CENTER);refresh.setBackground(material(a.card,a.glow,17));refresh.setOnClickListener(v->refreshApps());
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(46));rp.setMargins(0,dp(5),0,dp(5));root.addView(refresh,rp);
        TextView footer=label("Companion independiente • No es Recientes nativo",10,a.muted,false);footer.setGravity(Gravity.CENTER);root.addView(footer,new LinearLayout.LayoutParams(-1,dp(24)));
        setContentView(root);
    }
    private void choosePalette(){
        String[] names=new String[palettes.length];for(int i=0;i<names.length;i++)names[i]=palettes[i].name+" — "+palettes[i].desc;
        new AlertDialog.Builder(this).setTitle("Elegir paleta").setSingleChoiceItems(names,paletteIndex,(d,w)->{
            paletteIndex=w;prefs.edit().putInt(P_PALETTE,w).apply();d.dismiss();getWindow().setStatusBarColor(p().bg);getWindow().setNavigationBarColor(p().bg);buildShell();refreshApps();
        }).setNegativeButton("Cancelar",null).show();
    }
    private void chooseTexture(){
        new AlertDialog.Builder(this).setTitle("Elegir textura").setSingleChoiceItems(textures,textureIndex,(d,w)->{
            textureIndex=w;prefs.edit().putInt(P_TEXTURE,w).apply();d.dismiss();buildShell();refreshApps();
        }).setNegativeButton("Cancelar",null).show();
    }
    private static final class AppEntry {
        String name,pkg; Drawable icon;
        AppEntry(String n,String p,Drawable i){name=n;pkg=p;icon=i;}
    }
    private void refreshApps(){
        final int ticket=++generation;cardsRow.removeAllViews();countLabel.setText("Cargando aplicaciones en segundo plano…");
        worker.execute(()->{
            List<AppEntry> entries=new ArrayList<>();
            try{
                Intent q=new Intent(Intent.ACTION_MAIN);q.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> list=pm.queryIntentActivities(q,0);
                for(ResolveInfo ri:list)try{entries.add(new AppEntry(String.valueOf(ri.loadLabel(pm)),ri.activityInfo.packageName,ri.loadIcon(pm)));}catch(Exception ignored){}
                Collections.sort(entries,Comparator.comparing(e->e.name.toLowerCase()));
            }catch(Exception ignored){}
            final List<AppEntry> result=entries;
            main.post(()->{if(isFinishing()||ticket!=generation)return;countLabel.setText(result.size()+" aplicaciones • Desliza horizontalmente");renderBatch(result,ticket,0);});
        });
    }
    private void renderBatch(List<AppEntry> list,int ticket,int start){
        if(ticket!=generation||isFinishing())return;int end=Math.min(start+5,list.size());
        for(int i=start;i<end;i++)addCard(list.get(i),i);
        if(end<list.size())main.postDelayed(()->renderBatch(list,ticket,end),12);
        if(list.isEmpty())countLabel.setText("No hay aplicaciones disponibles");
    }
    private void addCard(AppEntry e,int index){
        Palette a=p();final String pkg=e.pkg,name=e.name;
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(13),dp(13),dp(13),dp(11));
        card.setBackground(material(a.card,a.glow,25));card.setElevation(dp(8));card.setRotationY(index%2==0?3f:-3f);card.setTranslationY(index%2==0?0:dp(8));
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(dp(218),dp(360));cp.setMargins(dp(2),dp(5),dp(12),dp(12));cardsRow.addView(card,cp);
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);TextView icon=new TextView(this);icon.setGravity(Gravity.CENTER);icon.setCompoundDrawablesWithIntrinsicBounds(e.icon,null,null,null);
        top.addView(icon,new LinearLayout.LayoutParams(dp(44),dp(44)));
        TextView close=label("×",24,a.accent,true);close.setGravity(Gravity.CENTER);top.addView(close,new LinearLayout.LayoutParams(0,dp(40),1));close.setOnClickListener(v->card.setVisibility(View.GONE));card.addView(top);
        TextView preview=label(name,21,a.ink,true);preview.setGravity(Gravity.CENTER);preview.setPadding(dp(8),dp(8),dp(8),dp(8));preview.setBackground(material(a.accent,a.glow,19));
        LinearLayout.LayoutParams vp=new LinearLayout.LayoutParams(-1,0,1);vp.setMargins(0,dp(11),0,dp(11));card.addView(preview,vp);
        TextView packageName=label(pkg,10,a.muted,false);packageName.setMaxLines(2);card.addView(packageName,new LinearLayout.LayoutParams(-1,dp(32)));
        TextView open=label("OPEN APP  ↗",12,a.ink,true);open.setGravity(Gravity.CENTER);open.setBackground(material(a.accent,a.glow,15));card.addView(open,new LinearLayout.LayoutParams(-1,dp(40)));
        View.OnClickListener launch=v->{Intent intent=pm.getLaunchIntentForPackage(pkg);if(intent!=null)try{startActivity(intent);}catch(Exception ex){Toast.makeText(this,"No se pudo abrir "+name,Toast.LENGTH_SHORT).show();}};
        open.setOnClickListener(launch);preview.setOnClickListener(launch);card.setOnClickListener(launch);
        card.setOnLongClickListener(v->{try{startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+pkg)));}catch(Exception ignored){}return true;});
        card.setContentDescription(name+". Toca para abrir; mantén pulsado para información.");
    }
    @Override protected void onDestroy(){generation++;worker.shutdownNow();main.removeCallbacksAndMessages(null);super.onDestroy();}

    private static final class MaterialDrawable extends Drawable {
        private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final int base,accent,radius;private final String texture;private final float density;
        MaterialDrawable(int b,int a,int r,String t,float den){base=b;accent=a;radius=r;texture=t;density=den;}
        private float d(float v){return v*density;}
        private int alpha(int c,int a){return (c&0x00ffffff)|((Math.max(0,Math.min(255,a))&255)<<24);}
        private int blend(int a,int b,float f){return Color.rgb((int)(Color.red(a)+(Color.red(b)-Color.red(a))*f),(int)(Color.green(a)+(Color.green(b)-Color.green(a))*f),(int)(Color.blue(a)+(Color.blue(b)-Color.blue(a))*f);}
        @Override public void draw(Canvas canvas){
            RectF r=new RectF(getBounds());if(r.width()<=0||r.height()<=0)return;
            int save=canvas.save();Path clip=new Path();clip.addRoundRect(r,radius,radius,Path.Direction.CW);canvas.clipPath(clip);
            paint.setStyle(Paint.Style.FILL);paint.setShader(new LinearGradient(r.left,r.top,r.right,r.bottom,base,blend(base,Color.BLACK,.2f),Shader.TileMode.CLAMP));canvas.drawRect(r,paint);paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(d(1));
            if(texture.equals("Metal cepillado")){
                paint.setColor(alpha(Color.WHITE,20));for(float y=r.top+2;y<r.bottom;y+=d(3))canvas.drawLine(r.left,y,r.right,y,paint);
            }else if(texture.equals("Cuero")){
                paint.setColor(alpha(accent,25));for(float y=r.top+5;y<r.bottom;y+=d(9))for(float x=r.left+4;x<r.right;x+=d(10))canvas.drawCircle(x,y,d(.8f),paint);
            }else if(texture.equals("Piedra")||texture.equals("Obsidiana")){
                paint.setColor(alpha(accent,texture.equals("Obsidiana")?48:30));
                for(int i=0;i<5;i++){float x=r.left+r.width()*(i+1)/6f;Path vein=new Path();vein.moveTo(x,r.top);vein.lineTo(x-r.width()*.08f,r.top+r.height()*.28f);vein.lineTo(x+r.width()*.04f,r.top+r.height()*.55f);vein.lineTo(x-r.width()*.05f,r.bottom);canvas.drawPath(vein,paint);}
            }else if(texture.equals("Papel")){
                paint.setColor(alpha(Color.WHITE,18));for(float y=r.top+4;y<r.bottom;y+=d(7))canvas.drawLine(r.left,y,r.right,y,paint);
            }else if(texture.equals("Agua")){
                paint.setColor(alpha(accent,50));for(int i=0;i<5;i++){float y=r.top+r.height()*(i+1)/6f;canvas.drawOval(r.left-r.width()*.15f,y-r.height()*.025f,r.right+r.width()*.15f,y+r.height()*.025f,paint);}
            }else if(texture.equals("Cristal facetado")){
                paint.setColor(alpha(accent,42));Path facet=new Path();facet.moveTo(r.left,r.top+r.height()*.3f);facet.lineTo(r.left+r.width()*.45f,r.top);facet.lineTo(r.right,r.top+r.height()*.22f);facet.lineTo(r.left+r.width()*.62f,r.bottom);facet.close();canvas.drawPath(facet,paint);
                paint.setColor(alpha(Color.WHITE,25));canvas.drawLine(r.left,r.top,r.right,r.bottom,paint);canvas.drawLine(r.right,r.top,r.left,r.bottom,paint);
            }else{
                paint.setColor(alpha(Color.WHITE,30));canvas.drawLine(r.left+r.width()*.12f,r.top,r.left+r.width()*.5f,r.bottom,paint);
                paint.setColor(alpha(accent,45));canvas.drawLine(r.left,r.top+r.height()*.18f,r.right,r.top+r.height()*.06f,paint);
            }
            paint.setColor(alpha(accent,105));canvas.drawRoundRect(r,radius,radius,paint);paint.setStyle(Paint.Style.FILL);canvas.restoreToCount(save);
        }
        @Override public void setAlpha(int a){paint.setAlpha(a);invalidateSelf();}
        @Override public void setColorFilter(android.graphics.ColorFilter f){paint.setColorFilter(f);invalidateSelf();}
        @Override public int getOpacity(){return android.graphics.PixelFormat.TRANSLUCENT;}
    }
}
