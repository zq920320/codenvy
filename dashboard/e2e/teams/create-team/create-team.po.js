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

  this.createTeamErrorMessage = $$('[mg-message]');

  this.addDevButtonElement = $('.che-list-add-button');

  // invite members popup

  this.memberDialogElement = $('.member-dialog-content');

  this.memberEmailsInputElement = helper.getVisibleInputElement(this.memberDialogElement.$('[che-name="email"]'));

  this.memberAddButtonElement = this.memberDialogElement.$('[che-button-title="Add"]');

  // invited developers list

  this.invitedListElement = $('.list-members');

  this.invitedListItemElements = this.invitedListElement.$$('.che-list-item');

  this.getInvitedDevListItemElement = (email) => {
    return this.invitedListItemElements.filter((elem, index) => {
      return elem.element(by.cssContainingText('span', email)).isDisplayed().then((isDisplayed) => {
        return isDisplayed;
      });
    }).get(0);
  };

  this.gotoCreatePage = () => {
    return this.navbarCreateTeamElement.click();
  };

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
