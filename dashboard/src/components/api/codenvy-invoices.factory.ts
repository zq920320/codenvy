/*
 *  [2015] - [2016] Codenvy, S.A.
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
import IResource = angular.resource.IResource;

export interface IInvoice {
  id: string;
  creationDate: number;
  totalPrice: number;
}

/**
 * This class is handling the invoices API.
 * @author Ann Shumilova
 */
export class CodenvyInvoices {
  $resource: ng.resource.IResourceService;
  $q: ng.IQService;
  $log: ng.ILogService;

  invoicesPerAccount: Map<string, any>;

  remoteInvoicesAPI: any;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($resource: ng.resource.IResourceService, $q: ng.IQService, $log: ng.ILogService) {
    this.$resource = $resource;
    this.$q = $q;
    this.$log = $log;

    this.invoicesPerAccount = new Map();

    // remote call
    this.remoteInvoicesAPI = this.$resource('/api/invoice/:accountId',{}, {
      getInvoices: {method: 'GET', url: '/api/invoice/:accountId'}
    });
  }

  /**
   * Fetch the invoices list.
   *
   * @param accountId {string} account's id
   * @return {ng.IPromise<any>}
   */
  fetchInvoices(accountId: string): ng.IPromise<any> {
    let promise = this.remoteInvoicesAPI.getInvoices({accountId: accountId}).$promise;

    let resultPromise = promise.then((data: IInvoice[]) => {
      this.invoicesPerAccount.set(accountId, data);
    });
    return resultPromise;
  }

  /**
   * Gets the list of invoices by account id.
   *
   * @param accountId {string}
   * @returns {Array}
   */
  getInvoices(accountId: string): IInvoice[] {
    //return this.invoicesPerAccount.get(accountId);
    let invoices = [];
    invoices.push({'id': 'id1', 'creationDate': 12300, 'totalPrice': 120});
    invoices.push({'id': 'id2', 'creationDate': 1287300, 'totalPrice': 125});
    invoices.push({'id': 'id3', 'creationDate': 1230085, 'totalPrice': 126});
    return invoices;
  }
}
