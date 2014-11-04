package com.linkedin.thirdeye.impl;

import com.linkedin.thirdeye.api.StarTreeRecord;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.FileReader;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class StarTreeRecordStreamAvroFileImpl implements Iterable<StarTreeRecord>
{
  private final File avroFile;
  private final List<String> dimensionNames;
  private final List<String> metricNames;
  private final String timeColumnName;

  public StarTreeRecordStreamAvroFileImpl(File avroFile,
                                          List<String> dimensionNames,
                                          List<String> metricNames,
                                          String timeColumnName) throws IOException
  {
    this.avroFile = avroFile;
    this.dimensionNames = dimensionNames;
    this.metricNames = metricNames;
    this.timeColumnName = timeColumnName;
  }

  @Override
  public Iterator<StarTreeRecord> iterator()
  {
    FileReader<GenericRecord> fileReader;
    try
    {
      fileReader = DataFileReader.openReader(avroFile, new GenericDatumReader<GenericRecord>());
    }
    catch (IOException e)
    {
      throw new IllegalStateException(e);
    }

    final Iterator<GenericRecord> itr = fileReader.iterator();

    return new Iterator<StarTreeRecord>()
    {
      @Override
      public boolean hasNext()
      {
        return itr.hasNext();
      }

      @Override
      public StarTreeRecord next()
      {
        StarTreeRecordImpl.Builder builder = new StarTreeRecordImpl.Builder();

        GenericRecord genericRecord = itr.next();

        // Extract dimensions
        for (String dimensionName : dimensionNames)
        {
          Object o = genericRecord.get(dimensionName);
          if (o == null)
          {
            throw new IllegalArgumentException("Found null dimension value in " + genericRecord);
          }
          builder.setDimensionValue(dimensionName, o.toString());
        }

        // Extract metrics
        for (String metricName : metricNames)
        {
          Object o = genericRecord.get(metricName);
          if (o == null)
          {
            throw new IllegalArgumentException("Found null metric value in " + genericRecord);
          }

          Schema.Field field = genericRecord.getSchema().getField(metricName);
          switch (field.schema().getType())
          {
            case INT:
              builder.setMetricValue(metricName, ((Integer) o).longValue());
              break;
            case LONG:
              builder.setMetricValue(metricName, (Long) o);
              break;
            default:
              throw new IllegalArgumentException("Metric field has invalid type " + field.schema().getType());
          }
        }

        // Extract time
        Object o = genericRecord.get(timeColumnName);
        if (o == null)
        {
          throw new IllegalArgumentException("Found null time value in " + genericRecord);
        }
        Schema.Field field = genericRecord.getSchema().getField(timeColumnName);
        switch (field.schema().getType())
        {
          case INT:
            builder.setTime(((Integer) o ).longValue());
            break;
          case LONG:
            builder.setTime((Long) o);
            break;
          default:
            throw new IllegalArgumentException("Time field has invalid type " + field.schema().getType());
        }

        return builder.build();
      }

      @Override
      public void remove()
      {
        itr.remove();
      }
    };
  }
}
