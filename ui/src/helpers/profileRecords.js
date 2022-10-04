/**
 * Detect and return the type of the passed argument as a string.
 *
 * Possible types: array, object, string, number, boolean
 *
 * If the type cannot be detected (for instance, when the value is null or undefined),
 * the function returns undefined.
 */
function getDataType(value) {
  if (Array.isArray(value)) {
    return "array";
  } else if (
    typeof value === "object" &&
    !Array.isArray(value) &&
    value !== null
  ) {
    return "object";
  } else if (typeof value === "string") {
    return "string";
  } else if (typeof value === "number") {
    return "number";
  } else if (typeof value === "boolean") {
    return "boolean";
  }

  return undefined;
}

/**
 * Determine the attributes of a list of records and profile them.
 *
 * Determine metrics, such as the number of distinct values, the
 * number of  missing values, and the most frequent values.
 */
export function profileRecords(records) {
  if (records === undefined) {
    return {};
  }

  const profileTemplate = {
    dataType: undefined,
    distinctValues: 0,
    frequencies: undefined,
    missingValues: 0,
    mostFrequentValues: undefined,
    totalValues: 0,
  };

  const profile = {};

  records.forEach(function (record) {
    Object.keys(record).forEach(function (attributeName) {
      const value = record[attributeName];
      if (profile[attributeName] === undefined) {
        profile[attributeName] = Object.assign({}, profileTemplate, {
          dataType: getDataType(value),
        });
        profile[attributeName].frequencies = new Map();
        profile[attributeName].mostFrequentValues = [];
      }

      // Detect data type
      if (profile[attributeName].dataType === undefined) {
        profile[attributeName].dataType = getDataType(value);
      }

      // Detect missing values
      if (value == null) {
        profile[attributeName].missingValues++;
      }

      if (profile[attributeName].frequencies.has(value)) {
        profile[attributeName].frequencies.set(
          value,
          profile[attributeName].frequencies.get(value) + 1
        );
      } else {
        profile[attributeName].frequencies.set(value, 1);
      }

      // Bump number of processed values
      profile[attributeName].totalValues++;
    });
  });

  // Determine most frequent values for each attribute
  const attributeNames = Object.keys(profile);
  attributeNames.forEach((attributeName) => {
    const sortedFrequencies = new Map(
      [...profile[attributeName].frequencies.entries()].sort(
        (a, b) => b[1] - a[1]
      )
    );
    const sortedFrequenciesKeys = sortedFrequencies.keys();
    for (let i = 0; i < 5; i++) {
      const value = sortedFrequenciesKeys.next().value;
      if (value === undefined) {
        break;
      }
      profile[attributeName].mostFrequentValues.push({
        value: value,
        count: profile[attributeName].frequencies.get(value),
      });
    }
  });

  return profile;
}
