package com.example.a553dswitcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Standalone multitasking companion; not Android's native Recents provider. */
public class MainActivity extends Activity {
    private static final String PREFS = "a55_ultra_preferences";
    private static final String PREF_PALETTE = "palette_index";

    private static final class Palette {
        final String name, description;
        final int background, backgroundMid, card, accent, glow, ink, muted;
        Palette(String n, String d, int bg, int mid, int c, int a, int g, int i, int m) {
            name=n; description=d; background=bg; backgroundMid=mid;
            card=c; accent=a; glow=g; ink=i; muted=m;
        }
    }

    private final Palette[] palettes = {
        new Palette("Obsidian", "Negro profundo y plata", 0xff080b12, 0xff171e2b, 0xff202a38, 0xffb9d9f5, 0xff66cfff, 0xfff0f6ff, 0xffa7b4c7),
        new Palette("Liquid Glass", "Cristal azul y cian", 0xff07131d, 0xff102a39, 0xff17384a, 0xffa6f2ff, 0xff28d9ff, 0xffeffcff, 0xffa7c5d4),
        new Palette("Aurora", "Verde y turquesa", 0xff071610, 0xff10291f, 0xff19392f, 0xffb2ffe1, 0xff47f5b0, 0xffedfff7, 0xffa5c7b8),
        new Palette("Cyberpunk", "Magenta y violeta", 0xff13091d, 0xff281333, 0xff382044, 0xffffc4f5, 0xfff04cff, 0xfffff1ff, 0xffd1b1d5),
        new Palette("Royal Purple", "Púrpura y lavanda", 0xff100b20, 0xff211735, 0xff30254c, 0xffe0d0ff, 0xffa783ff, 0xfff7f1ff, 0xffc0b3d9),
        new Palette("Arctic", "Hielo y azul eléctrico", 0xff07111c, 0xff14273a, 0xff24394d, 0xffe2f5ff, 0xff65afff, 0xfff5fbff, 0xffb1c4d8),
        new Palette("Rose Gold", "Rosa suave y cobre", 0xff1b1015, 0xff322029, 0xff49303a, 0xffffd8cf, 0xffff9dad, 0xfffff5f1, 0xffd3b4ba),
        new Palette("Emerald", "Esmeralda y jade", 0xff061711, 0xff0d2d22, 0xff174638, 0xffd0ffe8, 0xff38e5a4, 0xffedfff7, 0xffa5cdbb),
        new Palette("Solar Gold", "Ámbar y oro", 0xff191107, 0xff30220d, 0xff45331a, 0xffffe6a6, 0xffffc34d, 0xfffff8e8, 0xffd0bd91),
        new Palette("Crimson", "Rojo oscuro y rubí", 0xff18090d, 0xff30121a, 0xff48202b, 0xffffd4dc, 0xffff526f, 0xfffff1f3, 0xffd5aab4),
        new Palette("Pearl", "Perla y plata", 0xff151a21, 0xff252d38, 0xffdce5ef, 0xffffffff, 0xff9edbff, 0xff17212d, 0xff526274),
        new Palette("Ocean", "Azul marino y aqua", 0xff06111c, 0xff0d2639, 0xff16334b, 0xffb4f4ff, 0xff22bceb, 0xffeffbff, 0xffa4c4d7)
    };

    private final Set<String> hiddenThisSession = new HashSet<>();
    private LinearLayout root, cardsRow;
    private TextView countLabel, paletteButton;
    private List<ResolveInfo> launchables = new ArrayList<>();
    private PackageManager pm;
    private SharedPreferences prefs;
    private int paletteIndex = 1;

    private Palette theme() { return palettes[paletteIndex]; }
    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    private int darken(int c) {
        return Color.rgb((int)(Color.red(c)*0.72f), (int)(Color.green(c)*0.72f),
                (int)(Color.blue(c)*0.72f));
    }

    private GradientDrawable shape(int color, int stroke, int radiusDp) {
        GradientDrawable d = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR, new int[]{color, darken(color)});
        d.setCornerRadius(dp(radiusDp));
        d.setStroke(dp(1), stroke);
        return d;
    }

    private TextView label(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        pm = getPackageManager();
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        paletteIndex = Math.max(0, Math.min(palettes.length - 1,
                prefs.getInt(PREF_PALETTE, 1)));
        buildShell();
        refreshApps();
    }

    private void buildShell() {
        Palette p = theme();
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(16), dp(18), dp(12));
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{p.background, p.backgroundMid, p.background}));

        TextView eyebrow = label("A55  •  3D MULTITASKING ULTRA", 11, p.glow, true);
        root.addView(eyebrow, new LinearLayout.LayoutParams(-1, dp(24)));

        TextView title = label("Recent apps", 29, p.ink, true);
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(42)));

        paletteButton = label("◈  PALETA: " + p.name + "   ▾", 13, p.accent, true);
        paletteButton.setGravity(Gravity.CENTER);
        paletteButton.setBackground(shape(p.card, p.glow, 18));
        paletteButton.setOnClickListener(v -> showPalettePicker());
        LinearLayout.LayoutParams paletteLp = new LinearLayout.LayoutParams(-1, dp(44));
        paletteLp.setMargins(0, dp(6), 0, dp(8));
        root.addView(paletteButton, paletteLp);

        countLabel = label("Loading applications…", 12, p.muted, false);
        root.addView(countLabel, new LinearLayout.LayoutParams(-1, dp(28)));

        HorizontalScrollView horizontal = new HorizontalScrollView(this);
        horizontal.setHorizontalScrollBarEnabled(false);
        horizontal.setClipToPadding(false);
        horizontal.setPadding(dp(2), dp(16), dp(2), dp(16));
        cardsRow = new LinearLayout(this);
        cardsRow.setOrientation(LinearLayout.HORIZONTAL);
        horizontal.addView(cardsRow);
        root.addView(horizontal, new LinearLayout.LayoutParams(-1, 0, 1));

        TextView refresh = label("↻   Refresh apps", 14, p.accent, true);
        refresh.setGravity(Gravity.CENTER);
        refresh.setBackground(shape(p.card, p.glow, 18));
        refresh.setOnClickListener(v -> refreshApps());
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, dp(48));
        rp.setMargins(0, dp(6), 0, dp(6));
        root.addView(refresh, rp);

        TextView disclaimer = label("Independent companion • Not native One UI Recents", 10, p.muted, false);
        disclaimer.setGravity(Gravity.CENTER);
        root.addView(disclaimer, new LinearLayout.LayoutParams(-1, dp(26)));
        setContentView(root);
    }

    private void showPalettePicker() {
        String[] names = new String[palettes.length];
        for (int i = 0; i < palettes.length; i++)
            names[i] = palettes[i].name + "  —  " + palettes[i].description;
        new AlertDialog.Builder(this)
                .setTitle("Choose your color palette")
                .setSingleChoiceItems(names, paletteIndex, (dialog, which) -> {
                    paletteIndex = which;
                    prefs.edit().putInt(PREF_PALETTE, which).apply();
                    dialog.dismiss();
                    buildShell();
                    refreshApps();
                    Toast.makeText(this, palettes[which].name + " applied", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void refreshApps() {
        Intent query = new Intent(Intent.ACTION_MAIN);
        query.addCategory(Intent.CATEGORY_LAUNCHER);
        launchables = pm.queryIntentActivities(query, 0);
        Collections.sort(launchables, Comparator.comparing(
                ri -> String.valueOf(ri.loadLabel(pm)).toLowerCase()));
        cardsRow.removeAllViews();
        int shown = 0;
        for (ResolveInfo ri : launchables) {
            String pkg = ri.activityInfo.packageName;
            if (hiddenThisSession.contains(pkg)) continue;
            addCard(ri, shown++);
        }
        countLabel.setText(shown + " launchable apps  •  Swipe horizontally");
        if (shown == 0) countLabel.setText("No apps available to display");
    }

    private void addCard(ResolveInfo ri, int index) {
        final Palette p = theme();
        final String pkg = ri.activityInfo.packageName;
        final String appName = String.valueOf(ri.loadLabel(pm));
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(12));
        card.setBackground(shape(p.card, p.glow, 26));
        card.setElevation(dp(10));
        card.setRotationY(index == 0 ? -5f : (index % 2 == 0 ? 3.5f : -3f));
        card.setTranslationY(index % 2 == 0 ? 0 : dp(10));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(dp(222), dp(370));
        cp.setMargins(dp(2), dp(6), dp(14), dp(14));
        cardsRow.addView(card, cp);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView icon = new TextView(this);
        icon.setGravity(Gravity.CENTER);
        try { icon.setCompoundDrawablesWithIntrinsicBounds(ri.loadIcon(pm), null, null, null); }
        catch (Exception ignored) {}
        top.addView(icon, new LinearLayout.LayoutParams(dp(44), dp(44)));

        TextView close = label("×", 25, p.accent, true);
        close.setGravity(Gravity.CENTER);
        top.addView(close, new LinearLayout.LayoutParams(0, dp(42), 1));
        close.setOnClickListener(v -> {
            hiddenThisSession.add(pkg);
            refreshApps();
        });
        card.addView(top);

        TextView preview = label(appName, 22, p.ink, true);
        preview.setGravity(Gravity.CENTER);
        preview.setPadding(dp(8), dp(8), dp(8), dp(8));
        preview.setBackground(shape(p.accent, p.glow, 20));
        LinearLayout.LayoutParams vp = new LinearLayout.LayoutParams(-1, 0, 1);
        vp.setMargins(0, dp(12), 0, dp(12));
        card.addView(preview, vp);

        TextView packageName = label(pkg, 10, p.muted, false);
        packageName.setMaxLines(2);
        card.addView(packageName, new LinearLayout.LayoutParams(-1, dp(34)));

        TextView open = label("OPEN APP  ↗", 12, p.ink, true);
        open.setGravity(Gravity.CENTER);
        open.setBackground(shape(p.accent, p.glow, 16));
        card.addView(open, new LinearLayout.LayoutParams(-1, dp(42)));

        View.OnClickListener launch = v -> {
            Intent intent = pm.getLaunchIntentForPackage(pkg);
            if (intent != null) {
                try { startActivity(intent); }
                catch (Exception e) {
                    Toast.makeText(this, "Could not open " + appName, Toast.LENGTH_SHORT).show();
                }
            }
        };
        open.setOnClickListener(launch);
        preview.setOnClickListener(launch);
        card.setOnClickListener(launch);
        card.setOnLongClickListener(v -> {
            Intent info = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + pkg));
            startActivity(info);
            return true;
        });
        card.setContentDescription(appName + ". Tap to open; long press for app info.");
    }
}
