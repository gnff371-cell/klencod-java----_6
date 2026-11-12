package dkbd.zhxg;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private int tasbihCount = 0;
    private TextView tvTasbihDisplay;

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(0xFF0F172A);
        getWindow().setNavigationBarColor(0xFF0F172A);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(0xFF0F172A);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(20), dp(16), dp(24));
        scrollView.addView(root);

        // ترويسة التطبيق
        TextView appTitle = new TextView(this);
        appTitle.setText("أنا شيعي");
        appTitle.setTextSize(24);
        appTitle.setTextColor(0xFFF59E0B);
        appTitle.setTypeface(Typeface.DEFAULT_BOLD);
        appTitle.setGravity(Gravity.CENTER);
        root.addView(appTitle);

        TextView subTitle = new TextView(this);
        subTitle.setText("مواقيت الصلاة، الأدعية والزيارات، وتسبيحة الزهراء");
        subTitle.setTextSize(13);
        subTitle.setTextColor(0xFF94A3B8);
        subTitle.setGravity(Gravity.CENTER);
        subTitle.setPadding(0, dp(4), 0, dp(16));
        root.addView(subTitle);

        // بطاقة مواقيت الصلاة
        LinearLayout timesCard = new LinearLayout(this);
        timesCard.setOrientation(LinearLayout.VERTICAL);
        timesCard.setPadding(dp(14), dp(14), dp(14), dp(14));
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(0xFF1E293B);
        cardBg.setCornerRadius(dp(12));
        cardBg.setStroke(dp(1), 0xFF334155);
        timesCard.setBackground(cardBg);

        TextView cardTitle = new TextView(this);
        cardTitle.setText("المواقيت الشرعية (توقيت العراق / النجف وكربلاء)");
        cardTitle.setTextSize(15);
        cardTitle.setTextColor(0xFF38BDF8);
        cardTitle.setTypeface(Typeface.DEFAULT_BOLD);
        timesCard.addView(cardTitle);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        TextView tvDate = new TextView(this);
        tvDate.setText("تاريخ اليوم: " + sdf.format(new Date()));
        tvDate.setTextSize(12);
        tvDate.setTextColor(0xFF94A3B8);
        tvDate.setPadding(0, dp(2), 0, dp(8));
        timesCard.addView(tvDate);

        String[] prayers = {
                "• الفجر (الإمساك الشرعي): 05:08 ص",
                "• الشروق: 06:33 ص",
                "• الظهرين (الزوال الشرعي): 11:54 ص",
                "• فضيلة العصر: 02:50 م",
                "• المغرب (زوال الحمرة المشرقية): 05:25 م",
                "• العشاء: 06:15 م",
                "• منتصف الليل الشرعي: 11:17 م"
        };

        for (String p : prayers) {
            TextView tvP = new TextView(this);
            tvP.setText(p);
            tvP.setTextSize(14);
            tvP.setTextColor(Color.WHITE);
            tvP.setPadding(0, dp(2), 0, dp(2));
            timesCard.addView(tvP);
        }

        root.addView(timesCard);

        // زر تفعيل أذان المغرب
        Button btnAzan = makeButton("🔔 تفعيل تنبيه أذان المغرب القادم", 0xFF15803D);
        LinearLayout.LayoutParams btnAzanParams = new LinearLayout.LayoutParams(-1, dp(48));
        btnAzanParams.topMargin = dp(14);
        btnAzan.setOnClickListener(v -> scheduleMaghribAzan());
        root.addView(btnAzan, btnAzanParams);

        // زر إيقاف صوت الأذان
        Button btnStopAzan = makeButton("🔇 إيقاف صوت الأذان", 0xFFC62828);
        LinearLayout.LayoutParams stopParams = new LinearLayout.LayoutParams(-1, dp(48));
        stopParams.topMargin = dp(8);
        btnStopAzan.setOnClickListener(v -> {
            AzanReceiver.stopAzan();
            Toast.makeText(this, "تم إيقاف صوت الأذان", Toast.LENGTH_SHORT).show();
        });
        root.addView(btnStopAzan, stopParams);

        // قسم الأدعية والزيارات
        TextView praySection = new TextView(this);
        praySection.setText("الأدعية والزيارات");
        praySection.setTextSize(18);
        praySection.setTextColor(0xFFF59E0B);
        praySection.setTypeface(Typeface.DEFAULT_BOLD);
        praySection.setPadding(0, dp(20), 0, dp(10));
        root.addView(praySection);

        Button btnKumayl = makeButton("دعاء كميل بن زياد", 0xFF334155);
        btnKumayl.setOnClickListener(v -> showPrayerDialog(
                "دعاء كميل",
                "اللَّهُمَّ إِنِّي أَسْأَلُكَ بِرَحْمَتِكَ الَّتِي وَسِعَتْ كُلَّ شَيْءٍ، وَبِقُوَّتِكَ الَّتِي قَهَرْتَ بِهَا كُلَّ شَيْءٍ، وَخَضَعَ لَهَا كُلَّ شَيْءٍ، وَذَلَّ لَهَا كُلَّ شَيْءٍ، وَبِجَبَرُوتِكَ الَّتِي غَلَبْتَ بِهَا كُلَّ شَيْءٍ، وَبِعِزَّتِكَ الَّتِي لا يَقُومُ لَها شَيْءٌ..."));
        root.addView(btnKumayl, makeButtonParams());

        Button btnAshura = makeButton("زيارة عاشوراء", 0xFF334155);
        btnAshura.setOnClickListener(v -> showPrayerDialog(
                "زيارة عاشوراء",
                "السَّلامُ عَلَيْكَ يا أَبا عَبْدِ اللهِ، السَّلامُ عَلَيْكَ يَابْنَ رَسُولِ اللهِ، السَّلامُ عَلَيْكَ يَابْنَ أَمِيرِ الْمُؤْمِنِينَ وَابْنَ سَيِّدِ الْوَصِيِّينَ، السَّلامُ عَلَيْكَ يَابْنَ فاطِمَةَ سَيِّدَةِ نِساءِ العالَمِينَ، السَّلامُ عَلَيْكَ يا ثارَ اللهِ وَابْنَ ثارِهِ وَالْوِتْرَ الْمَوْتُورَ..."));
        root.addView(btnAshura, makeButtonParams());

        Button btnTawassul = makeButton("دعاء التوسل بالأئمة الأطهار (ع)", 0xFF334155);
        btnTawassul.setOnClickListener(v -> showPrayerDialog(
                "دعاء التوسل",
                "اللَّهُمَّ إِنِّي أَسْأَلُكَ وَأَتَوَجَّهُ إِلَيْكَ بِنَبِيِّكَ نَبِيِّ الرَّحْمَةِ مُحَمَّدٍ صَلَّى اللهُ عَلَيْهِ وَآلِهِ، يا أَبَا القاسِمِ، يا رَسُولَ اللهِ، يا إِمامَ الرَّحْمَةِ، يا سَيِّدَنا وَمَوْلانا، إِنّا تَوَجَّهْنا وَاسْتَشْفَعْنا وَتَوَسَّلْنا بِكَ إِلَى اللهِ، وَقَدَّمْناكَ بَيْنَ يَدَيْ حاجاتِنا، يا وَجِيهاً عِنْدَ اللهِ اشْفَعْ لَنا عِنْدَ اللهِ..."));
        root.addView(btnTawassul, makeButtonParams());

        Button btnAlYasin = makeButton("زيارة آل يس", 0xFF334155);
        btnAlYasin.setOnClickListener(v -> showPrayerDialog(
                "زيارة آل يس",
                "سَلامٌ عَلى آلِ ياسِينَ، السَّلامُ عَلَيْكَ يا داعِيَ اللهِ وَرَبّانِيَّ آياتِهِ، السَّلامُ عَلَيْكَ يا بابَ اللهِ وَدَيّانَ دِينِهِ، السَّلامُ عَلَيْكَ يا خَلِيفَةَ اللهِ وَناصِرَ حَقِّهِ، السَّلامُ عَلَيْكَ يا حُجَّةَ اللهِ وَدَلِيلَ إِرادَتِهِ..."));
        root.addView(btnAlYasin, makeButtonParams());

        // قسم تسبيحة الزهراء (ع)
        TextView tasbihSection = new TextView(this);
        tasbihSection.setText("تسبيحة الزهراء (عليها السلام)");
        tasbihSection.setTextSize(18);
        tasbihSection.setTextColor(0xFFF59E0B);
        tasbihSection.setTypeface(Typeface.DEFAULT_BOLD);
        tasbihSection.setPadding(0, dp(20), 0, dp(10));
        root.addView(tasbihSection);

        tvTasbihDisplay = new TextView(this);
        tvTasbihDisplay.setText("العدد: 0 | البداية: الله أكبر (34)");
        tvTasbihDisplay.setTextSize(16);
        tvTasbihDisplay.setTextColor(0xFF38BDF8);
        tvTasbihDisplay.setGravity(Gravity.CENTER);
        tvTasbihDisplay.setPadding(0, dp(6), 0, dp(10));
        root.addView(tvTasbihDisplay);

        Button btnTasbih = makeButton("سَبِّحْ (اضغط هنا)", 0xFF0284C7);
        btnTasbih.setOnClickListener(v -> stepTasbih());
        root.addView(btnTasbih, makeButtonParams());

        Button btnResetTasbih = makeButton("إعادة تعيين العداد", 0xFF475569);
        btnResetTasbih.setOnClickListener(v -> {
            tasbihCount = 0;
            tvTasbihDisplay.setText("العدد: 0 | البداية: الله أكبر (34)");
        });
        root.addView(btnResetTasbih, makeButtonParams());

        setContentView(scrollView);
    }

    private LinearLayout.LayoutParams makeButtonParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(46));
        params.topMargin = dp(8);
        return params;
    }

    private Button makeButton(String text, int color) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setTextSize(14);
        btn.setTextColor(Color.WHITE);
        btn.setAllCaps(false);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(10));
        btn.setBackground(bg);
        return btn;
    }

    private void stepTasbih() {
        tasbihCount++;
        String currentZikr;

        if (tasbihCount <= 34) {
            currentZikr = "الله أكبر (" + tasbihCount + "/34)";
        } else if (tasbihCount <= 67) {
            currentZikr = "الحمد لله (" + (tasbihCount - 34) + "/33)";
        } else if (tasbihCount <= 100) {
            currentZikr = "سبحان الله (" + (tasbihCount - 67) + "/33)";
        } else {
            tasbihCount = 1;
            currentZikr = "الله أكبر (1/34)";
        }

        tvTasbihDisplay.setText(currentZikr);

        Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vib != null) {
            vib.vibrate(30);
        }
    }

    private void scheduleMaghribAzan() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 40);
        cal.set(Calendar.SECOND, 0);

        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(this, AzanReceiver.class);
        intent.putExtra("PRAYER_NAME", "المغرب");

        PendingIntent pi = PendingIntent.getBroadcast(
                this,
                101,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
                } else {
                    am.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
                }
                Toast.makeText(this, "✅ تم ضبط موعد أذان المغرب بنجاح!", Toast.LENGTH_SHORT).show();
            } catch (SecurityException e) {
                Toast.makeText(this, "⚠️ يعمل التنبيه العادي (الرجاء السماح بالمنبهات الدقيقة)", Toast.LENGTH_LONG).show();
                try {
                    am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
                } catch (Exception ignored) {}
            }
        }
    }

    private void showPrayerDialog(String title, String content) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton("إغلاق", null)
                .show();
    }

    public static class AzanReceiver extends BroadcastReceiver {
        public static MediaPlayer mediaPlayer;

        public static void stopAzan() {
            if (mediaPlayer != null) {
                try {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                    mediaPlayer.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mediaPlayer = null;
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String prayerName = intent.getStringExtra("PRAYER_NAME");
            if (prayerName == null) prayerName = "الصلاة";

            Toast.makeText(context, "حان الآن أذان " + prayerName + " حسب التوقيت المحلي", Toast.LENGTH_LONG).show();

            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(2000);
            }

            try {
                stopAzan();
                mediaPlayer = MediaPlayer.create(context, R.raw.azan_karbala);
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}