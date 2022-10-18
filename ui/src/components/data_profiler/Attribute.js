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
    const field = this.props.field;
    const dataSourceProfile = this.props.dataSourceProfile;

    return (
      <li>
        <div className="card mb-4 shadow-sm data-source-profile-attribute">
          <div className="card-header">
            <h5 className="my-0">{field.name}</h5>
          </div>
          <div className="card-body">
            <table>
              <tbody>
                <tr>
                  <td className="attribute-name">Data Type:</td>
                  <td className="attribute-value">
                    {this.capitalizeString(field.dataType)}
                  </td>
                </tr>
                {field.nonNullValues !== undefined && (
                  <React.Fragment>
                    <tr>
                      <td className="attribute-name">Null Values?:</td>
                      <td className="attribute-value">
                        {field.nonNullValues < dataSourceProfile.records
                          ? "Yes"
                          : "No"}
                      </td>
                    </tr>
                    <tr>
                      <td className="attribute-name">Non-Null Values:</td>
                      <td className="attribute-value">
                        {this.formatStat(field.nonNullValues)}
                      </td>
                    </tr>
                    <tr>
                      <td className="attribute-name">Null Values:</td>
                      <td className="attribute-value">
                        {this.formatStat(
                          dataSourceProfile.records - field.nonNullValues
                        )}
                      </td>
                    </tr>
                  </React.Fragment>
                )}
                {field.distinctValues !== undefined && (
                  <tr>
                    <td className="attribute-name">Distinct Values:</td>
                    <td className="attribute-value">
                      {this.formatStat(field.distinctValues)}
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
            {!["bytes", "bytes array", "null", "string"].includes(
              field.dataType
            ) && (
              <table>
                <tbody>
                  {field.minValue !== undefined && (
                    <tr>
                      <td className="attribute-name">Minimum:</td>
                      <td className="attribute-value">
                        {this.formatStat(field.minValue)}
                      </td>
                    </tr>
                  )}
                  {field.maxValue !== undefined && (
                    <tr>
                      <td className="attribute-name">Maximum:</td>
                      <td className="attribute-value">
                        {this.formatStat(field.maxValue)}
                      </td>
                    </tr>
                  )}
                  {field.medianValue !== undefined && (
                    <tr>
                      <td className="attribute-name">Median:</td>
                      <td className="attribute-value">
                        {this.formatStat(field.medianValue)}
                      </td>
                    </tr>
                  )}
                  {field.averageValue !== undefined && (
                    <tr>
                      <td className="attribute-name">Average:</td>
                      <td className="attribute-value">
                        {this.formatStat(field.averageValue)}
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
