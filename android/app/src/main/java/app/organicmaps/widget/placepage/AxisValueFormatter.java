package app.organicmaps.widget.placepage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import app.organicmaps.sdk.Framework;

public class AxisValueFormatter implements IAxisValueFormatter
{
  @NonNull
  private final BarLineChartBase mChart;

  public AxisValueFormatter(@NonNull BarLineChartBase chart)
  {
    super();
    mChart = chart;
  }

  @Override
  public String getFormattedValue(float value, @Nullable AxisBase axisBase)
  {
    return Framework.nativeFormatAltitude(value);
  }
}
