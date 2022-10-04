import React, { Component } from "react";

class Breadcrumb extends Component {
  render() {
    const items = this.props.items;
    return (
      <div className="col-12 mt-3">
        <nav aria-label="breadcrumb">
          <ol className="breadcrumb mb-0">
            {items.map((item, idx) => (
              <React.Fragment key={idx}>
                {item.uri && (
                  <li className="breadcrumb-item">
                    <a href={item.uri}>{item.name}</a>
                  </li>
                )}
                {!item.uri && (
                  <li className="breadcrumb-item active" aria-current="page">
                    {item.name}
                  </li>
                )}
              </React.Fragment>
            ))}
          </ol>
        </nav>
      </div>
    );
  }
}

export default Breadcrumb;
