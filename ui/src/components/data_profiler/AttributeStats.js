import React, { Component } from "react";

class AttributeStats extends Component {
  formatNumber(value) {
    return parseFloat(value).toLocaleString();
  }

  renderPercentageStat(value, count, percentage) {
    return (
      <li className={"stat-percentage stat-percentage-" + percentage}>
        <span className="label">{value}</span>
        <span className="value">
          {this.formatNumber(count)} ({percentage} %)
        </span>
      </li>
    );
  }

  render() {
    const profile = this.props.attributeProfile;
    if (profile === undefined) {
      return <ul></ul>;
    }

    const distinctValuesRatio =
      profile.totalValues > 0
        ? ((profile.distinctValues / profile.totalValues) * 100).toFixed(0)
        : undefined;
    const missingValuesRatio =
      profile.totalValues > 0
        ? ((profile.missingValues / profile.totalValues) * 100).toFixed(0)
        : undefined;

    return (
      <ul>
        {this.renderPercentageStat(
          "Distinct Values:",
          profile.distinctValues,
          distinctValuesRatio
        )}
        {this.renderPercentageStat(
          "Missing Values:",
          profile.missingValues,
          missingValuesRatio
        )}
      </ul>
    );
  }
}

export default AttributeStats;
