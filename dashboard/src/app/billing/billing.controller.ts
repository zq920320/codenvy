/*
 *  [2015] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
'use strict';
import {CodenvyPayment, ICreditCard} from '../../components/api/codenvy-payment.factory';
import {BillingService} from './billing.service';
import {CodenvyTeam} from '../../components/api/codenvy-team.factory';

enum Tab {Summary, Card, Invoices}

export class BillingController {
  $log: ng.ILogService;
  $q: ng.IQService;
  cheAPI: any;
  codenvyPayment: CodenvyPayment;
  codenvyTeam: CodenvyTeam;
  cheNotification: any;
  billingService: BillingService;

  creditCard: ICreditCard;
  origCreditCard: ICreditCard;
  cardInfoForm: ng.IFormController;
  selectedTabIndex: number;
  accountId: string;
  loading: boolean;

  tab: Object = Tab;

  /**
   * @ngInject for Dependency injection
   */
  constructor ($log: ng.ILogService, $q: ng.IQService, cheAPI: any, codenvyPayment: CodenvyPayment,
               codenvyTeam: CodenvyTeam,cheNotification: any, billingService: BillingService) {
    this.$log = $log;
    this.$q = $q;
    this.cheAPI = cheAPI;
    this.codenvyPayment = codenvyPayment;
    this.codenvyTeam = codenvyTeam;
    this.cheNotification = cheNotification;
    this.billingService = billingService;

    this.accountId = '';

    this.selectedTabIndex = Tab.Summary;

    this.fetchCreditCard();
  }

  /**
   * Fetches account ID.
   *
   * @return {IPromise<any>}
   */
  fetchAccountId(): ng.IPromise<any> {
    return this.codenvyTeam.fetchTeams().then(() => {
      this.accountId = this.codenvyTeam.getPersonalAccount().id;
    }, (error: any) => {
      if (error.status === 304) {
        this.accountId = this.codenvyTeam.getPersonalAccount().id;
      }
    });
  }

  /**
   * Fetches and stores a credit card.
   *
   */
  fetchCreditCard(): ng.IPromise<any> {
    this.loading = true;

    return this.fetchAccountId().then(() => {
      return this.billingService.fetchCreditCard(this.accountId);
    }).then((creditCard: ICreditCard) => {
      this.creditCard = creditCard;
      this.origCreditCard = angular.copy(this.creditCard);
    }).finally(() => {
      this.loading = false;
    });
  }

  /**
   * Gets credit card and creates a copy
   */
  getCreditCard(): void {
    this.creditCard = this.billingService.getCreditCard(this.accountId);
    this.origCreditCard = angular.copy(this.creditCard);
  }

  /**
   * Callback when credit card has been changed.
   *
   * @param creditCard {ICreditCard}
   */
  creditCardChanged(creditCard: ICreditCard): void {
    this.creditCard = angular.copy(creditCard);
  }

  /**
   * Deletes existing credit card.
   */
  creditCardDeleted(): void {
    this.loading = true;

    this.billingService.removeCreditCard(this.creditCard.accountId, this.creditCard.token).then(() => {
      return this.fetchCreditCard();
    },(error: any) => {
      this.cheNotification.showError(error && error.data && error.data.message ? error.data.message : 'Failed to delete the credit card.');
    }).finally(() => {
      this.loading = false;
    });
  }

  /**
   * Adds new credit card or updates an existing one.
   */
  saveCard(): void {
    this.loading = true;

    let savePromise;
    if (this.creditCard.token) {
      // update exiting card
      savePromise = this.billingService.updateCreditCard(this.accountId, this.creditCard);
    } else {
      // add new card
      savePromise = this.billingService.addCreditCard(this.accountId, this.creditCard);
    }

    savePromise.then(() => {
      return this.fetchCreditCard();
    }, (error: any) => {
      this.cheNotification.showError(error && error.data && error.data.message ? error.data.message : 'Failed to save the credit card.');
    }).finally(() => {
      this.loading = false;
    });
  }

  /**
   * Cancels credit card information changes
   */
  cancelCard(): void {
    this.creditCard = angular.copy(this.origCreditCard);
  }

  /**
   * Registers card info form
   * Gets a credit card
   *
   * @param form {ng.IFormController}
   */
  onCreditCardSelect(form: ng.IFormController): void {
    this.cardInfoForm = form;
    this.getCreditCard();
  }

  /**
   * Returns true if form on Card tab is valid
   *
   * @return {boolean}
   */
  isSaveButtonDisabled(): boolean {
    return !(this.cardInfoForm && this.cardInfoForm.$valid)
      || angular.equals(this.creditCard, this.origCreditCard);
  }

  /**
   * Returns true if "Save" button should be visible
   *
   * @return {boolean}
   */
  isSaveButtonVisible(): boolean {
    return this.selectedTabIndex === Tab.Card && !this.loading;
  }
}
