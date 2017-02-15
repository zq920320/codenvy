'use strict';

let Helpers = function() {

  this.getVisibleInputElement = (containerElement) => {
    return containerElement.$$('input').filter((elem, index) => {
      return elem.isDisplayed().then((isDisplayed) => {
        return isDisplayed;
      });
    }).get(0);
  };

  this.getRandomName = (name) => {
    return name + (('0000' + (Math.random() * Math.pow(36, 4) << 0).toString(36)).slice(-4));
  };

  this.getDevs = () => {
    if (!browser.params.devs) {
      return [];
    }
    return browser.params.devs.split(' ');
  };

  // pair means 'email:password'
  this.getEmail = (pair) => {
    return pair.split(':')[0];
  };

  // pair means 'email:password'
  this.getPassword = (pair) => {
    return pair.split(':')[1];
  };

};

module.exports = new Helpers();
