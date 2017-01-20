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

interface ICreditCardElement extends ng.IAugmentedJQuery {
  card: Function;
}

/**
 * Defines a directive for creating a credit card component.
 * @author Oleksii Kurinnyi
 */
export class AddCreditCard {
  restrict: string = 'E';
  replace: boolean = false;
  templateUrl: string = '/app/billing/card-info/add-credit-card/add-credit-card.html';

  bindToController: boolean = true;

  controller: string = 'AddCreditCardController';
  controllerAs: string = 'addCreditCardController';

  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
    this.scope = {
      creditCard: '='
    };
  }

  link($scope: ng.IScope, $element: ICreditCardElement): void {
    ($element.find('.addCreditCardForm') as ICreditCardElement).card({
      // a selector or jQuery object for the container
      // where you want the card to appear
      container: '.card-wrapper', // *required*
      numberInput: 'input[name="deskcardNumber"]', // optional — default input[name="number"]
      expiryInput: 'input[name="deskexpires"]', // optional — default input[name="expiry"]
      cvcInput: 'input[name="deskcvv"]', // optional — default input[name="cvc"]
      nameInput: 'input[name="deskcardholder"]', // optional - defaults input[name="name"]

      // width: 200, // optional — default 350px
      formatting: true // optional - default true
    });
  }

}
