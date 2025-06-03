package app.organicmaps.universalbuttons;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import app.organicmaps.MwmApplication;
import app.organicmaps.R;
import app.organicmaps.help.HelpActivity;

public class UniversalButtonsHolder
{
  private static volatile UniversalButtonsHolder instance;

  public static final String KEY_PREF_UNIVERSAL_BUTTON = "universal_button";
  private static final String BUTTON_HELP_CODE = "help";
  public static final String BUTTON_SETTINGS_CODE = "settings";
  public static final String BUTTON_ADD_PLACE_CODE = "add-place";
  public static final String BUTTON_RECORD_TRACK_CODE = "record-track";
  private static final String DEFAULT_BUTTON_CODE = BUTTON_HELP_CODE;

  private final Context context;
  private final SharedPreferences prefs;
  private final Map<String, UniversalButton> availableButtons = new HashMap<>();

  private UniversalButtonsHolder(Context context)
  {
    this.context = context;
    this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
  }

  public void registerButton(UniversalButton button)
  {
    availableButtons.put(button.getCode(), button);
  }

  public void registerDefaultUniversalButtons(Context context)
  {
    registerButton(new UniversalButton()
    {
      @Override
      public String getCode()
      {
        return BUTTON_HELP_CODE;
      }

      @Override
      public String getPrefsName()
      {
        return context.getString(R.string.help);
      }

      @Override
      public void drawIcon(FloatingActionButton imageView)
      {
        imageView.setImageResource(R.drawable.ic_question_mark);
      }

      @Override
      public void onClick(FloatingActionButton universalButtonView)
      {
        Intent intent = new Intent(context, HelpActivity.class);
        context.startActivity(intent);
      }
    });
  }

  @Nullable
  public UniversalButton getActiveButton()
  {
    String activeButtonCode = prefs.getString(KEY_PREF_UNIVERSAL_BUTTON, DEFAULT_BUTTON_CODE);
    if (!TextUtils.isEmpty(activeButtonCode))
      return availableButtons.get(activeButtonCode);
    else
      return null;
  }

  public Collection<UniversalButton> getAllButtons()
  {
    return availableButtons.values();
  }

  public static UniversalButtonsHolder getInstance(Context context)
  {
    UniversalButtonsHolder localInstance = instance;
    if (localInstance == null)
    {
      synchronized (UniversalButtonsHolder.class)
      {
        localInstance = instance;
        if (localInstance == null)
        {
          instance = localInstance = new UniversalButtonsHolder(context);
        }
      }
    }
    return localInstance;
  }
}
