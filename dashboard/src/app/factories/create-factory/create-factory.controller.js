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

/**
 * Controller for a create factory.
 * @author Oleksii Orel
 */
export class CreateFactoryCtrl {

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($scope, $location, $log, codenvyAPI, cheNotification) {
    this.$location = $location;
    this.$log = $log;
    this.codenvyAPI = codenvyAPI;
    this.cheNotification = cheNotification;

    this.isLoading = false;
    this.isImporting = false;

    this.factoryContent = null;

    this.factoryName = '';
    this.factoryMetadataForm = null;

    $scope.$watch(() => {
      return this.factoryContent;
    }, () => {
      if (this.factoryMetadataForm.$dirty && this.factoryName) {
        // user has already set the factory's name
        // so update factory content
        this.onFactoryNameChange();
      } else if (this.factoryMetadataForm.$pristine) {
        // factory name is empty or it was set automatically
        // so try to set factory name from factory content if it present there
        this.onFactoryContentChange();
      }
    })
  }

  /**
   * Create a new factory by factory content
   * @param factoryContent
   */
  createFactoryByContent(factoryContent) {
    if (!factoryContent) {
      return;
    }
    this.isImporting = true;

    let promise = this.codenvyAPI.getFactory().createFactoryByContent(factoryContent);

    promise.then((factory) => {
      this.isImporting = false;
      this.cheNotification.showInfo('Factory successfully created.');
      this.$location.path('/factory/' + factory.id);
    }, (error) => {
      this.isImporting = false;
      this.cheNotification.showError(error.data.message ? error.data.message : 'Create factory failed.');
      this.$log.error(error);
    });
  }

  /**
   * Updates factory content on factory name change
   */
  onFactoryNameChange() {
    if (!this.factoryContent) {
      return;
    }
    let factoryContentObject = angular.fromJson(this.factoryContent);
    factoryContentObject.name = this.factoryName;
    this.factoryContent = angular.toJson(factoryContentObject, true);
  }

  /**
   * Updates factory name on factory content change
   */
  onFactoryContentChange() {
    if (!this.factoryContent) {
      return;
    }
    let factoryContentObject = angular.fromJson(this.factoryContent);
    this.factoryName = factoryContentObject.name || '';
  }

  setFactoryMetadataForm(form) {
    this.factoryMetadataForm = form;
  }
}
