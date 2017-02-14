'use strict';

let DeleteTeamPageObject = function() {

  this.openBillingsPage = () => {
    return $('.navbar-account-section').click().then(() => {
      let billingMenuItemElement = element(by.cssContainingText('.navbar-click-area', 'Billing'));
      return billingMenuItemElement.click();
    });
  };

  this.deleteTeamButton = $('[che-name="team-details-delete-team"]').$('button');

  this.deleteTeam = (teamName) => {
    browser.setLocation('/team/'+ teamName);
    browser.waitForAngular();
    this.deleteTeamButton.click().then(()=>{
      browser.waitForAngular();
      $('che-popup[title="Remove team"]').$('che-button-primary[che-button-title="Delete"]').click();

    });
  };

  this.findBillingTeamElements = () => {
    this.listTeamsElement = $('.list-teams-content');
    this.listHeaderElement = this.listTeamsElement.$('.che-list-header');
    this.listBodyElement = this.listTeamsElement.$('.che-list-content');
    this.deleteButtonElement = this.listTeamsElement.$('[che-button-title="Delete"]');
  };

  this.deleteAllTeams = () => {
    browser.setLocation('/billing');
    this.findBillingTeamElements();
    this.listBodyElement.all(by.repeater('team in listTeamsController.teams')).count().then((count) => {
      if (count) {
        this.listHeaderElement.$('md-checkbox').click().then(() => {
          this.deleteButtonElement.click().then(() => {
            $('che-popup[title="Remove teams"]').$('che-button-primary[che-button-title="Delete"]').click();
          })
        });
      }
    });
  };

};

module.exports = new DeleteTeamPageObject();
