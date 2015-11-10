describe('EntitiesService', function() {
    var
        propertyNew = {
            name: 'new',
            type: {clazz: 'String'},
            value: '123'
        },
        propertyModified = {
            name: 'modified',
            type: {clazz: 'Long'},
            value: 123
        },
        propertyDeleted = {
            name: 'deleted',
            type: {clazz: 'Long'},
            value: 123
        },
        propertyOld = {
            name: 'old',
            type: {clazz: 'Long'},
            value: 123
        };

    var service;

    beforeEach(function() {
        module('xodus');
        inject(function(_EntitiesService_) {
            service = _EntitiesService_;
        });
    });

    it('not generate change summary for not modified fields', function() {
        var changeSummary = service.getChangeSummary(entity(propertyModified), entity(propertyModified));
        checkEmpty(changeSummary.properties);
        checkEmpty(changeSummary.links);
    });

    it('generate change summary for modified field value', function() {
        var property = angular.copy(propertyModified);
        property.value = property.value + '1';
        var changeSummary = service.getChangeSummary(entity(propertyModified), entity(property));
        expect(changeSummary.properties.added).toEqual([]);
        expect(changeSummary.properties.modified).toEqual([property]);
        expect(changeSummary.properties.deleted).toEqual([]);

        checkEmpty(changeSummary.links);
    });

    it('generate change summary for modified typed field', function() {
        var property = angular.copy(propertyModified);
        property.type = service.allTypes()[3];
        var changeSummary = service.getChangeSummary(entity(propertyModified), entity(property));
        expect(changeSummary.properties.added).toEqual([property]);
        expect(changeSummary.properties.modified).toEqual([]);
        expect(changeSummary.properties.deleted).toEqual([propertyModified]);

        checkEmpty(changeSummary.links);
    });

    it('generate change summary for modified typed field', function() {
        var property = angular.copy(propertyModified);
        property.type = service.allTypes()[3];
        var changeSummary = service.getChangeSummary(entity(propertyModified), entity(property));
        expect(changeSummary.properties.added).toEqual([property]);
        expect(changeSummary.properties.modified).toEqual([]);
        expect(changeSummary.properties.deleted).toEqual([propertyModified]);

        checkEmpty(changeSummary.links);
    });

    it('generate change summary for deleted field', function() {
        var changeSummary = service.getChangeSummary(entity([propertyOld, propertyDeleted]),
            entity(propertyOld));
        expect(changeSummary.properties.added).toEqual([]);
        expect(changeSummary.properties.modified).toEqual([]);
        expect(changeSummary.properties.deleted).toEqual([propertyDeleted]);

        checkEmpty(changeSummary.links);
    });

    it('generate change summary for added field', function() {

        var changeSummary = service.getChangeSummary(entity(propertyOld),
            entity([propertyOld, propertyNew]));
        expect(changeSummary.properties.added).toEqual([propertyNew]);
        expect(changeSummary.properties.modified).toEqual([]);
        expect(changeSummary.properties.deleted).toEqual([]);

        checkEmpty(changeSummary.links);
    });

    it('generate change summary for added/deleted/modified fields', function() {
        var property = angular.copy(propertyModified);
        property.value = property.value + '1';
        var changeSummary = service.getChangeSummary(entity([propertyOld, propertyModified]),
            entity([property, propertyNew]));
        expect(changeSummary.properties.added).toEqual([propertyNew]);
        expect(changeSummary.properties.modified).toEqual([property]);
        expect(changeSummary.properties.deleted).toEqual([propertyOld]);

        checkEmpty(changeSummary.links);
    });

    function entity(property) {
        if (angular.isArray(property)) {
            return {
                properties: angular.copy(property),
                links: []
            }
        }
        return {
            properties: [angular.copy(property)],
            links: []
        }
    }

    function checkEmpty(changeSummaryItem) {
        expect(changeSummaryItem.added).toEqual([]);
        expect(changeSummaryItem.modified).toEqual([]);
        expect(changeSummaryItem.deleted).toEqual([]);
    }

});