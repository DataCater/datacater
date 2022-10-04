import React, { Component } from "react";

class Attribute extends Component {
  // TODO: move into common helper class
  formatStat(metric) {
    // apply formatting only to integers
    if (metric === parseInt(metric, 10)) {
      return parseInt(metric, 10).toLocaleString();
    } else if (typeof metric === "string") {
      if (metric.length > 10) {
        return metric.substring(0, 10) + "..";
      } else {
        return metric;
      }
    } else {
      return metric;
    }
  }

  // TODO: move into common helper class
  capitalizeString(string) {
    const capString = string.charAt(0).toUpperCase() + string.slice(1);
    return capString.length > 10
      ? capString.substring(0, 10) + ".."
      : capString;
  }

  render() {
    const attribute = this.props.attribute;
    const dataSourceProfile = this.props.dataSourceProfile;

    return (
      <li>
        <div className="card mb-4 shadow-sm data-source-profile-attribute">
          <div className="card-header">
            <h5 className="my-0">{attribute.name}</h5>
          </div>
          <div className="card-body">
            <table>
              <tbody>
                <tr>
                  <td className="attribute-name">Data Type:</td>
                  <td className="attribute-value">
                    {this.capitalizeString(attribute.dataType)}
                  </td>
                </tr>
                {attribute.nonNullValues !== undefined && (
                  <React.Fragment>
                    <tr>
                      <td className="attribute-name">Null Values?:</td>
                      <td className="attribute-value">
                        {attribute.nonNullValues < dataSourceProfile.records
                          ? "Yes"
                          : "No"}
                      </td>
                    </tr>
                    <tr>
                      <td className="attribute-name">Non-Null Values:</td>
                      <td className="attribute-value">
                        {this.formatStat(attribute.nonNullValues)}
                      </td>
                    </tr>
                    <tr>
                      <td className="attribute-name">Null Values:</td>
                      <td className="attribute-value">
                        {this.formatStat(
                          dataSourceProfile.records - attribute.nonNullValues
                        )}
                      </td>
                    </tr>
                  </React.Fragment>
                )}
                {attribute.distinctValues !== undefined && (
                  <tr>
                    <td className="attribute-name">Distinct Values:</td>
                    <td className="attribute-value">
                      {this.formatStat(attribute.distinctValues)}
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
            {!["bytes", "bytes array", "null", "string"].includes(
              attribute.dataType
            ) && (
              <table>
                <tbody>
                  {attribute.minValue !== undefined && (
                    <tr>
                      <td className="attribute-name">Minimum:</td>
                      <td className="attribute-value">
                        {this.formatStat(attribute.minValue)}
                      </td>
                    </tr>
                  )}
                  {attribute.maxValue !== undefined && (
                    <tr>
                      <td className="attribute-name">Maximum:</td>
                      <td className="attribute-value">
                        {this.formatStat(attribute.maxValue)}
                      </td>
                    </tr>
                  )}
                  {attribute.medianValue !== undefined && (
                    <tr>
                      <td className="attribute-name">Median:</td>
                      <td className="attribute-value">
                        {this.formatStat(attribute.medianValue)}
                      </td>
                    </tr>
                  )}
                  {attribute.averageValue !== undefined && (
                    <tr>
                      <td className="attribute-name">Average:</td>
                      <td className="attribute-value">
                        {this.formatStat(attribute.averageValue)}
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            )}
          </div>
        </div>
      </li>
    );
  }
}

export default Attribute;
