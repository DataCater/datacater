class Profiler {
  profileAttribute(values, dataType) {
    const profile = {
      distinctValues: 0,
      maximumValue: undefined,
      meanValue: undefined,
      medianValue: undefined,
      minimumValue: undefined,
      missingValues: 0,
      mostFrequentValues: [],
      standardDeviation: undefined,
      totalValues: 0,
      variance: undefined,
    };

    const frequencies = new Map();
    const rawValues = [];
    const addToFrequencies = function (value) {
      if (value == null) {
        profile.missingValues++;
      }
      if (frequencies.has(value)) {
        frequencies.set(value, frequencies.get(value) + 1);
      } else {
        profile.distinctValues++;
        frequencies.set(value, 1);
      }
      rawValues.push(value);
      profile.totalValues++;
    };
    let valuesSum = 0;

    values.forEach(function (value) {
      if (value == null) {
        addToFrequencies(null);
      } else if (value.constructor === Array) {
        value.forEach(function (nestedValue) {
          if (dataType === "date") {
            addToFrequencies(nestedValue.toLocaleDateString());
          } else if (
            ["timestamp-micros", "timestamp-millis"].includes(dataType)
          ) {
            addToFrequencies(nestedValue.toLocaleString());
          } else if (["time-micros", "time-millis"].includes(dataType)) {
            addToFrequencies(nestedValue.toLocaleTimeString());
          } else if (["int", "long", "float", "double"].includes(dataType)) {
            addToFrequencies(nestedValue);
            valuesSum += nestedValue;
          } else {
            addToFrequencies(nestedValue);
          }
        });
      } else {
        if (dataType === "date") {
          addToFrequencies(value.toLocaleDateString());
        } else if (
          ["timestamp-micros", "timestamp-millis"].includes(dataType)
        ) {
          addToFrequencies(value.toLocaleString());
        } else if (["time-micros", "time-millis"].includes(dataType)) {
          addToFrequencies(value.toLocaleTimeString());
        } else if (["int", "long", "float", "double"].includes(dataType)) {
          addToFrequencies(value);
          valuesSum += value;
        } else {
          addToFrequencies(value);
        }
      }
    });

    if (["int", "long", "float", "double"].includes(dataType)) {
      const rawPresentValues = rawValues
        .filter((_) => _ != null)
        .sort((a, b) => a - b);
      const presentValuesCount = rawPresentValues.length;
      profile.minimumValue =
        presentValuesCount > 0 ? rawPresentValues[0] : undefined;
      profile.maximumValue =
        presentValuesCount > 0
          ? rawPresentValues[presentValuesCount - 1]
          : undefined;
      // implement calculation of mean value more efficiently (in loop above)
      profile.meanValue = valuesSum / presentValuesCount;
      let half = Math.floor(presentValuesCount / 2);
      if (profile.totalValues % 2) {
        profile.medianValue = rawPresentValues[half];
      } else {
        profile.medianValue =
          (rawPresentValues[half - 1] + rawPresentValues[half]) / 2.0;
      }

      // calculate standard deviation
      let deviationSum = 0;
      rawPresentValues.forEach(function (value) {
        deviationSum +=
          (value - profile.meanValue) * (value - profile.meanValue);
      });
      profile.variance = deviationSum / presentValuesCount;
      profile.standardDeviation = Math.sqrt(profile.variance);
    }

    const sortedFrequencies = new Map(
      [...frequencies.entries()].sort((a, b) => b[1] - a[1])
    );
    const sortedFrequenciesKeys = sortedFrequencies.keys();
    for (let i = 0; i < 5; i++) {
      const value = sortedFrequenciesKeys.next().value;
      if (value === undefined) {
        break;
      }
      profile.mostFrequentValues.push({
        value: value,
        count: frequencies.get(value),
      });
    }

    return profile;
  }
}

export default Profiler;
