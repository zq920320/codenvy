'use strict';

let CreateTeamPageObject = function() {
  let helper = require('../../helpers');
  let navbarPageObject = require('../../navbar/navbar.po');

  this.navbarCreateTeamElement = navbarPageObject.teamsElement.$('a[href$="team/create"]');

  this.navbarTeamsListElement = navbarPageObject.teamsElement.$('.navbar-teams-list');
  this.navbarTeamsListItemElements = element.all(by.repeater('team in navbarTeamsController.getTeams()'));

  this.createTeamForm = $('ng-form[name="createTeamForm"]');

  this.newTeamNameElement = helper.getVisibleInputElement(this.createTeamForm.$('[che-name="name"]'));

  this.createTeamButtonElement = $('#create-team-button');

  this.createTeam = (name) => {
    this.navbarCreateTeamElement.click().then (()=>{
      this.newTeamNameElement.clear();
      this.newTeamNameElement.sendKeys(name).then(() => {
        this.createTeamButtonElement.click();
      });
    });
    browser.waitForAngular();
  }
};

module.exports = new CreateTeamPageObject();
