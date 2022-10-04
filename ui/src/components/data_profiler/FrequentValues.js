import React, { Component } from "react";

class FrequentValues extends Component {
  renderPercentageStat(rawValue, count, percentage, idx) {
    if (typeof rawValue == "string") {
      const tokens = rawValue.split(" ");

      if (tokens.length === 0) {
        return (
          <li
            className={"stat-percentage stat-percentage-" + percentage}
            key={idx}
          >
            <span className="label">{rawValue}</span>
            <span className="value">
              {count} ({percentage} %)
            </span>
          </li>
        );
      } else {
        return (
          <li
            className={"stat-percentage stat-percentage-" + percentage}
            key={idx}
          >
            <span className="label">
              {tokens.map((value, index) => (
                <React.Fragment key={index}>
                  {index > 0 && <span className="text-black-50">·</span>}
                  {value}
                </React.Fragment>
              ))}
            </span>
            <span className="value">
              {count} ({percentage} %)
            </span>
          </li>
        );
      }
    } else {
      let value = rawValue;
      if (rawValue === true) {
        value = "true";
      } else if (rawValue === false) {
        value = "false";
      } else if (rawValue == null) {
        value = "null";
      }

      return (
        <li
          className={"stat-percentage stat-percentage-" + percentage}
          key={idx}
        >
          <span className="label">{value}</span>
          <span className="value">
            {count} ({percentage} %)
          </span>
        </li>
      );
    }
  }

  render() {
    const profile = this.props.attributeProfile;

    if (profile === undefined) {
      return <ul></ul>;
    }

    return (
      <ul>
        {profile.mostFrequentValues.map((mfv, idx) =>
          this.renderPercentageStat(
            mfv.value,
            mfv.count,
            ((mfv.count / profile.totalValues) * 100).toFixed(0),
            idx
          )
        )}
      </ul>
    );
  }
}

export default FrequentValues;
