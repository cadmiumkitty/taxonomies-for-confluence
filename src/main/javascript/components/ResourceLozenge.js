import PropTypes from 'prop-types';
import React from 'react';

class ResourceLozenge extends React.Component {

  constructor(props) {
    super(props);
  }

  render() {
    
    const { label, type, appearance } = this.props;

    var backgroundImage = (appearance == 'error' || appearance == 'empty')
      ? 'icons/macro-resource-unknown-disabled-01.svg'
      : type == 'http://www.w3.org/2000/01/rdf-schema#Class' 
        ? 'icons/macro-resource-class-01.svg'
        : type == 'http://www.w3.org/1999/02/22-rdf-syntax-ns#Property' 
          ? 'icons/macro-resource-property-01.svg'
          : type == 'http://www.w3.org/2004/02/skos/core#Concept'
            ? 'icons/macro-resource-concept-01.svg'
            : 'icons/macro-resource-unknown-disabled-01.svg';

    const color = (appearance == 'error' || appearance == 'empty')
      ? '#6B778C'
      : '#172B4D';
    
    const borderColor = (appearance == 'error' || appearance == 'empty')
      ? '#EBECF0'
      : type == 'http://www.w3.org/2000/01/rdf-schema#Class' 
        ? '#00B8D9'
        : type == 'http://www.w3.org/1999/02/22-rdf-syntax-ns#Property' 
          ? '#00B8D9'
          : type == 'http://www.w3.org/2004/02/skos/core#Concept'
            ? '#00875A'
            : '#EBECF0';
      
    return (
      <span style={{
        backgroundImage: `url(${backgroundImage})`,
        backgroundColor: '#FFFFFF',
        backgroundRepeat: 'no-repeat',
        backgroundPosition: '4px 50%',
        color: `${color}`,
        borderRadius: '3px',
        border: `2px solid ${borderColor}`,
        padding: '1px 4px 1px 24px',
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        boxSizing: 'border-box'
      }}>{ label }</span>

    );
  }
}

ResourceLozenge.propTypes = {
  label: PropTypes.string.isRequired,
  appearance: PropTypes.string.isRequired
}

export default ResourceLozenge