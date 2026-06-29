package com.riffforge.feature_widgets.presentation

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.riffforge.R

class QuickActionsWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_quick_actions)

        val tunerIntent = Intent(Intent.ACTION_VIEW, Uri.parse("riffforge://tuner"))
        val tunerPendingIntent = PendingIntent.getActivity(
            context,
            0,
            tunerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_tuner, tunerPendingIntent)

        val dailyIntent = Intent(Intent.ACTION_VIEW, Uri.parse("riffforge://daily"))
        val dailyPendingIntent = PendingIntent.getActivity(
            context,
            1,
            dailyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_daily, dailyPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}