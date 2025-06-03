package app.organicmaps.universalbuttons;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public interface UniversalButton
{
  String getCode();

  String getPrefsName();

  void drawIcon(FloatingActionButton imageView);

  void onClick(FloatingActionButton universalButtonView);
}
