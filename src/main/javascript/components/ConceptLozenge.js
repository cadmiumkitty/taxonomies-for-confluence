import PropTypes from 'prop-types';
import React from 'react';

class ConceptLozenge extends React.Component {

  constructor(props) {
    super(props);
  }

  render() {
    
    const { label, predicate, appearance } = this.props;

    const predicatePatern = predicate == 'http://purl.org/dc/terms/relation'
      ? '-relation'
      : predicate == 'http://purl.org/dc/terms/type'
        ? '-type'
        : '-subject';

    const appearancePatern = (appearance == 'error' || appearance == 'empty')
    ? '-disabled'
    : '';

    const backgroundImage = `icons/macro${predicatePatern}${appearancePatern}-01.svg`;
    const color = (appearance == 'error' || appearance == 'empty')
      ? '#6B778C'
      : '#172B4D';
    const borderColor = (appearance == 'error' || appearance == 'empty')
      ? '#EBECF0'
      : '#00875A';
      
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

ConceptLozenge.propTypes = {
  label: PropTypes.string.isRequired,
  predicate: PropTypes.string.isRequired,
  appearance: PropTypes.string.isRequired
}

export default ConceptLozenge