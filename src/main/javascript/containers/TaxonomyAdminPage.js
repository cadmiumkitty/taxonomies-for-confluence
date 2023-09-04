import _ from 'lodash';

import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import Tabs, { Tab, TabList, TabPanel } from '@atlaskit/tabs';

import { getTaxonomyVersions } from '../actions/TaxonomyVersions';

import TaxonomyVersions from './TaxonomyVersions';
import TaxonomyAdminSkosTaxonomies from './TaxonomyAdminSkosTaxonomies';
import TaxonomyAdminRdfsClasses from './TaxonomyAdminRdfsClasses';
import TaxonomyAdminRdfsProperties from './TaxonomyAdminRdfsProperties';

class TaxonomyAdminPage extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
    };
  }

  componentDidMount = () => {
    const { dispatch } = this.props;
    dispatch(getTaxonomyVersions(true));
  }

  render() {
    return (
      <div style = {{ overflow: 'hidden', height: '100%' }} id = "ac-iframe-options" data-options = "resize:false;sizeToParent:true">
        <TaxonomyVersions/>
        <Tabs>
          <TabList>
            <Tab>Taxonomies</Tab>
            <Tab>Classes</Tab>
            <Tab>Properties</Tab>
          </TabList>
          <TabPanel>
            <TaxonomyAdminSkosTaxonomies/>
          </TabPanel>
          <TabPanel>
            <TaxonomyAdminRdfsClasses/>
          </TabPanel>
          <TabPanel>
          <TaxonomyAdminRdfsProperties/>
          </TabPanel>
        </Tabs>
      </div>
    );
  }
}

TaxonomyAdminPage.propTypes = {
  dispatch: PropTypes.func.isRequired
}

const mapStateToProps = state => {
  return {
  };
}

export default connect(mapStateToProps)(TaxonomyAdminPage);