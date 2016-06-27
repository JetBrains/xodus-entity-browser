describe('EntitiesService', function() {

    describe('properties change summary', function() {
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

        it('not generate change summary for not modified properties', function() {
            var changeSummary = service.getChangeSummary(entity(propertyModified), entity(propertyModified));
            checkEmpty(changeSummary.properties);
            checkEmpty(changeSummary.links);
        });

        it('generate change summary for modified property value', function() {
            var property = angular.copy(propertyModified);
            property.value = property.value + '1';
            var changeSummary = service.getChangeSummary(entity(propertyModified), entity(property));
            expect(changeSummary.properties.added).toEqual([]);
            expect(changeSummary.properties.modified).toEqual([property]);
            expect(changeSummary.properties.deleted).toEqual([]);

            checkEmpty(changeSummary.links);
        });

        it('generate change summary for modified typed property', function() {
            var property = angular.copy(propertyModified);
            property.type = service.allTypes()[3];
            var changeSummary = service.getChangeSummary(entity(propertyModified), entity(property));
            expect(changeSummary.properties.added).toEqual([property]);
            expect(changeSummary.properties.modified).toEqual([]);
            expect(changeSummary.properties.deleted).toEqual([propertyModified]);

            checkEmpty(changeSummary.links);
        });

        it('generate change summary for deleted property', function() {
            var changeSummary = service.getChangeSummary(entity([propertyOld, propertyDeleted]),
                entity(propertyOld));
            expect(changeSummary.properties.added).toEqual([]);
            expect(changeSummary.properties.modified).toEqual([]);
            expect(changeSummary.properties.deleted).toEqual([propertyDeleted]);

            checkEmpty(changeSummary.links);
        });

        it('generate change summary for new property', function() {

            var changeSummary = service.getChangeSummary(entity(propertyOld),
                entity([propertyOld, propertyNew]));
            expect(changeSummary.properties.added).toEqual([propertyNew]);
            expect(changeSummary.properties.modified).toEqual([]);
            expect(changeSummary.properties.deleted).toEqual([]);

            checkEmpty(changeSummary.links);
        });

        it('generate change summary for added/deleted/modified properties', function() {
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
    });

    describe('links change summary', function() {
        var
            newLink = {
                name: 'new',
                typeId: '1',
                entityId: '1'
            },
            modifiedLink = {
                name: 'modified',
                typeId: '1',
                entityId: '2'
            },
            deletedLink = {
                name: 'deleted',
                typeId: '1',
                entityId: '3'
            },
            oldLink = {
                name: 'old',
                typeId: '1',
                entityId: '4'
            };

        var service;

        beforeEach(function() {
            module('xodus');
            inject(function(_EntitiesService_) {
                service = _EntitiesService_;
            });
        });

        it('not generate change summary for not modified links', function() {
            var changeSummary = service.getChangeSummary(entity(modifiedLink), entity(modifiedLink));
            checkEmpty(changeSummary.properties);
            checkEmpty(changeSummary.links);
        });

        it('generate change summary for modified link value', function() {
            var link = angular.copy(modifiedLink);
            link.entityId = link.entityId + '1';
            var changeSummary = service.getChangeSummary(entity(modifiedLink), entity(link));
            expect(changeSummary.links.added).toEqual([]);
            expect(changeSummary.links.modified).toEqual([link]);
            expect(changeSummary.links.deleted).toEqual([]);

            checkEmpty(changeSummary.properties);
        });

        it('generate change summary for deleted link', function() {
            var changeSummary = service.getChangeSummary(entity([oldLink, deletedLink]), entity(oldLink));
            expect(changeSummary.links.added).toEqual([]);
            expect(changeSummary.links.modified).toEqual([]);
            expect(changeSummary.links.deleted).toEqual([deletedLink]);

            checkEmpty(changeSummary.properties);
        });

        it('generate change summary for new link', function() {
            var changeSummary = service.getChangeSummary(entity(oldLink), entity([oldLink, newLink]));
            expect(changeSummary.links.added).toEqual([newLink]);
            expect(changeSummary.links.modified).toEqual([]);
            expect(changeSummary.links.deleted).toEqual([]);

            checkEmpty(changeSummary.properties);
        });

        it('generate change summary for added/deleted/modified links', function() {
            var link = angular.copy(modifiedLink);
            link.entityId = link.entityId + '1';
            var changeSummary = service.getChangeSummary(entity([oldLink, modifiedLink]),
                entity([link, newLink]));
            expect(changeSummary.links.added).toEqual([newLink]);
            expect(changeSummary.links.modified).toEqual([link]);
            expect(changeSummary.links.deleted).toEqual([oldLink]);

            checkEmpty(changeSummary.properties);
        });

        function entity(link) {
            if (angular.isArray(link)) {
                return {
                    properties: [],
                    links: angular.copy(link)
                }
            }
            return {
                links: [angular.copy(link)],
                properties: []
            }
        }

    });

    function checkEmpty(changeSummaryItem) {
        expect(changeSummaryItem.added).toEqual([]);
        expect(changeSummaryItem.modified).toEqual([]);
        expect(changeSummaryItem.deleted).toEqual([]);
    }

});