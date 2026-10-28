package com.example.a553dswitcher;

import android.app.Activity;
import android.content.Intent;
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

/**
 * A launcher-style multitasking companion, not Android's system Recents provider.
 * It intentionally does not request usage access, accessibility, root, or Shizuku.
 */
public class MainActivity extends Activity {
    private final Set<String> hiddenThisSession = new HashSet<>();
    private LinearLayout cardsRow;
    private TextView countLabel;
    private List<ResolveInfo> launchables = new ArrayList<>();
    private PackageManager pm;

    private int dp(float v) {
        return (int)(v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable bg(int color, int stroke) {
        GradientDrawable d = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{color, darken(color)}
        );
        d.setCornerRadius(dp(26));
        d.setStroke(dp(1), stroke);
        return d;
    }

    private int darken(int c) {
        return Color.rgb((int)(Color.red(c)*0.72), (int)(Color.green(c)*0.72),
                        (int)(Color.blue(c)*0.72));
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
        getWindow().setStatusBarColor(Color.rgb(7, 10, 16));
        getWindow().setNavigationBarColor(Color.rgb(7, 10, 16));
        buildShell();
        refreshApps();
    }

    private void buildShell() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(20), dp(18), dp(12));
        root.setBackground(new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{0xff070a11, 0xff171e2c, 0xff05070b}));

        TextView eyebrow = label("SAMSUNG • MULTITASKING CONCEPT", 11, 0xff8ce9ff, true);
        root.addView(eyebrow, new LinearLayout.LayoutParams(-1, dp(26)));
        TextView title = label("Recent apps", 29, Color.WHITE, true);
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(44)));
        countLabel = label("Loading applications…", 12, 0xffa8b4c6, false);
        root.addView(countLabel, new LinearLayout.LayoutParams(-1, dp(28)));

        HorizontalScrollView horizontal = new HorizontalScrollView(this);
        horizontal.setHorizontalScrollBarEnabled(false);
        horizontal.setClipToPadding(false);
        horizontal.setPadding(dp(2), dp(22), dp(2), dp(20));
        cardsRow = new LinearLayout(this);
        cardsRow.setOrientation(LinearLayout.HORIZONTAL);
        horizontal.addView(cardsRow);
        root.addView(horizontal, new LinearLayout.LayoutParams(-1, 0, 1));

        TextView refresh = label("↻   Refresh apps", 14, 0xffdceaff, true);
        refresh.setGravity(Gravity.CENTER);
        refresh.setBackground(bg(0xff1a2637, 0xff47738e));
        refresh.setOnClickListener(v -> refreshApps());
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, dp(48));
        rp.setMargins(0, dp(8), 0, dp(6));
        root.addView(refresh, rp);

        TextView disclaimer = label("Independent companion • Not native One UI Recents", 10,
                                    0xff8794a7, false);
        disclaimer.setGravity(Gravity.CENTER);
        root.addView(disclaimer, new LinearLayout.LayoutParams(-1, dp(28)));
        setContentView(root);
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
        String pkg = ri.activityInfo.packageName;
        String appName = String.valueOf(ri.loadLabel(pm));
        int[] palette = {0xffe4eef6, 0xffdce9e3, 0xffe9e0f2, 0xfff0e4d7, 0xffdce7f3};
        int cardColor = palette[index % palette.length];
        int ink = 0xff17212d;

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(12));
        card.setBackground(bg(cardColor, 0x9987dfff));
        card.setElevation(dp(14));
        card.setRotationY(index == 0 ? -5f : index % 2 == 0 ? 4f : -3f);
        card.setTranslationY(index % 2 == 0 ? dp(0) : dp(16));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(dp(222), dp(390));
        cp.setMargins(dp(2), dp(6), dp(14), dp(16));
        cardsRow.addView(card, cp);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView icon = new TextView(this);
        icon.setGravity(Gravity.CENTER);
        try { icon.setCompoundDrawablesWithIntrinsicBounds(ri.loadIcon(pm), null, null, null); }
        catch (Exception ignored) {}
        icon.setText("");
        top.addView(icon, new LinearLayout.LayoutParams(dp(44), dp(44)));
        TextView close = label("×", 25, ink, true);
        close.setGravity(Gravity.CENTER);
        top.addView(close, new LinearLayout.LayoutParams(0, dp(42), 1));
        close.setOnClickListener(v -> {
            hiddenThisSession.add(pkg);
            refreshApps();
        });
        card.addView(top);

        TextView preview = label(appName, 22, ink, true);
        preview.setGravity(Gravity.CENTER);
        preview.setBackground(bg(0xfff4f7fa, 0x6687a8c1));
        LinearLayout.LayoutParams vp = new LinearLayout.LayoutParams(-1, 0, 1);
        vp.setMargins(0, dp(12), 0, dp(12));
        card.addView(preview, vp);

        TextView packageName = label(pkg, 10, 0xff526274, false);
        packageName.setMaxLines(2);
        card.addView(packageName, new LinearLayout.LayoutParams(-1, dp(34)));

        TextView open = label("OPEN APP  ↗", 12, ink, true);
        open.setGravity(Gravity.CENTER);
        open.setBackground(bg(0xffc7dce9, 0x5587a8c1));
        card.addView(open, new LinearLayout.LayoutParams(-1, dp(42)));
        View.OnClickListener launch = v -> {
            Intent intent = pm.getLaunchIntentForPackage(pkg);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
