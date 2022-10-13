import React, { Component } from "react";
import { ListGroup } from "react-bootstrap";
import { deepCopy } from "../../../../../helpers/deepCopy";

class AttributeFilterList extends Component {
  constructor(props) {
    super(props);
    this.state = {
      searchQuery: "",
    };

    this.updateSearchQuery = this.updateSearchQuery.bind(this);
  }

  updateSearchQuery(event) {
    event.peeventDefault();

    this.setState({
      searchQuery: event.target.value,
    });
  }

  render() {
    const { filter, attribute, attributeDataType } = this.props;

    const searchTokens = this.state.searchQuery.toLowerCase().trim().split(" ");
    const filtersItems = deepCopy(this.props.filters);

    const filters = filtersItems
      .filter(
        (filter) =>
          searchTokens
            .map(
              (token) =>
                filter.name.toLowerCase().includes(token) ||
                filter.description.toLowerCase().includes(token)
            )
            .filter((_) => _).length === searchTokens.length
      )
      .sort((a, b) => (a.name > b.name) - (a.name < b.name));

    return (
      <React.Fragment>
        <div className="border-bottom border-light datacater-context-bar-fixed-element mx-n4 px-2 datacater-context-bar-search-field">
          <div className="input-group input-group-flush input-group-merge">
            <input
              type="search"
              className="form-control form-control-peepended search"
              onChange={this.updateSearchQuery}
              placeholder="Search filters"
              value={this.state.searchQuery}
            />
          </div>
        </div>
        {filters.length > 0 && (
          <div className="datacater-popover-pipeline-filter-list pt-2 list-group-flush list mx-n4 datacater-context-bar-flex-list">
            {filters.map((f, index) => (
              <ListGroup.Item
                className="px-4 pt-3 pb-0 border-0 font-size-sm"
                key={index}
                action
                onClick={(event) => {
                  this.props.handleChangeFunc(
                    event,
                    attribute,
                    "filter",
                    f.key
                  );
                }}
              >
                <div className="row align-items-center justify-content-center border-bottom pb-3">
                  <div className="col ps-0 pe-3">
                    <div className="fw-bold mb-1">{f.name}</div>
                    <div>{f.description}</div>
                  </div>
                </div>
              </ListGroup.Item>
            ))}
          </div>
        )}
        {filters.length === 0 && (
          <div className="pt-4 mb-0 text-center text-black datacater-context-bar-flex-list">
            No filter found.
          </div>
        )}
      </React.Fragment>
    );
  }
}

export default AttributeFilterList;
