# Webhooks

## Events

### Creation of property

Event: `content_created`

#### Query

`?lic=none`

#### Body

```
{
  "id": "1188593679",
  "type": "com.atlassian.confluence.plugins.confluence-content-property-storage:content-property",
  "status": "current",
  "title": "subject",
  "space": {
    "id": 1149861892,
    "key": "~323528440",
    "name": "Eugene Morozov",
    "type": "personal",
    "status": "current",
    "_expandable": {
      "settings": "/rest/api/space/~323528440/settings",
      "metadata": "",
      "operations": "",
      "lookAndFeel": "/rest/api/settings/lookandfeel?spaceKey=~323528440",
      "identifiers": "",
      "permissions": "",
      "icon": "",
      "description": "",
      "theme": "/rest/api/space/~323528440/theme",
      "history": "",
      "homepage": "/rest/api/content/1149862011"
    },
    "_links": {
      "webui": "/spaces/~323528440",
      "self": "https://dalstonsemantics.atlassian.net/wiki/rest/api/space/~323528440"
    }
  },
  "history": {
    "latest": true,
    "createdBy": {
      "type": "known",
      "accountId": "557058:d092c978-5ab7-4c8a-8d87-32ffac22c584",
      "accountType": "atlassian",
      "email": "eugene.morozov@dalstonsemantics.com",
      "publicName": "Eugene Morozov",
      "profilePicture": {
        "path": "/wiki/aa-avatar/557058:d092c978-5ab7-4c8a-8d87-32ffac22c584",
        "width": 48,
        "height": 48,
        "isDefault": false
      },
      "displayName": "Eugene Morozov",
      "isExternalCollaborator": false,
      "_expandable": {
        "operations": "",
        "personalSpace": ""
      },
      "_links": {
        "self": "https://dalstonsemantics.atlassian.net/wiki/rest/api/user?accountId=557058:d092c978-5ab7-4c8a-8d87-32ffac22c584"
      }
    },
    "createdDate": "2021-09-14T07:39:42.016Z",
    "_expandable": {
      "lastUpdated": "",
      "previousVersion": "",
      "contributors": "",
      "nextVersion": ""
    },
    "_links": {
      "self": "https://dalstonsemantics.atlassian.net/wiki/rest/api/content/1188593679/history"
    }
  },
  "version": {
    "by": {
      "type": "known",
      "accountId": "557058:d092c978-5ab7-4c8a-8d87-32ffac22c584",
      "accountType": "atlassian",
      "email": "eugene.morozov@dalstonsemantics.com",
      "publicName": "Eugene Morozov",
      "profilePicture": {
        "path": "/wiki/aa-avatar/557058:d092c978-5ab7-4c8a-8d87-32ffac22c584",
        "width": 48,
        "height": 48,
        "isDefault": false
      },
      "displayName": "Eugene Morozov",
      "isExternalCollaborator": false,
      "_expandable": {
        "operations": "",
        "personalSpace": ""
      },
      "_links": {
        "self": "https://dalstonsemantics.atlassian.net/wiki/rest/api/user?accountId=557058:d092c978-5ab7-4c8a-8d87-32ffac22c584"
      }
    },
    "when": "2021-09-14T07:39:42.016Z",
    "friendlyWhen": "just a moment ago",
    "message": "",
    "number": 1,
    "minorEdit": false,
    "contentTypeModified": false,
    "_expandable": {
      "collaborators": "",
      "content": "/rest/api/content/1188593679"
    },
    "_links": {
      "self": "https://dalstonsemantics.atlassian.net/wiki/rest/api/content/1188593679/version/1"
    }
  },
  "macroRenderedOutput": {},
  "metadata": {
    "labels": {
      "results": [],
      "start": 0,
      "limit": 200,
      "size": 0,
      "_links": {
        "next": "/rest/api/content/1188593679/label?next=true&limit=200&start=200",
        "self": "https://dalstonsemantics.atlassian.net/wiki/rest/api/content/1188593679/label"
      }
    },
    "_expandable": {
      "currentuser": "",
      "comments": "",
      "simple": "",
      "properties": "",
      "frontend": "",
      "likes": ""
    }
  },
  "_expandable": {
    "childTypes": "",
    "container": "/rest/api/content/1188560897",
    "operations": "",
    "schedulePublishDate": "",
    "children": "/rest/api/content/1188593679/child",
    "restrictions": "/rest/api/content/1188593679/restriction/byOperation",
    "ancestors": "",
    "body": "",
    "descendants": "/rest/api/content/1188593679/descendant"
  },
  "_links": {
    "context": "/wiki",
    "self": "https://dalstonsemantics.atlassian.net/wiki/rest/api/content/1188593679",
    "collection": "/rest/api/content",
    "webui": "",
    "base": "https://dalstonsemantics.atlassian.net/wiki"
  }
}
```

### Update of property

Event: `content_updated`

#### Body

```
{
  "id": "1188593679",
  "type": "com.atlassian.confluence.plugins.confluence-content-property-storage:content-property",
  "status": "current",
  "title": "subject",
  "space": {
    "id": 1149861892,
    "key": "~323528440",
    "name": "Eugene Morozov",
    "type": "personal",
    "status": "current",
    "_expandable": {
      "settings": "/rest/api/space/~323528440/settings",
      "metadata": "",
      "operations": "",
      "lookAndFeel": "/rest/api/settings/lookandfeel?spaceKey=~323528440",
      "identifiers": "",
      "permissions": "",
      "icon": "",
      "description": "",
      "theme": "/rest/api/space/~323528440/theme",
      "history": "",
      "homepage": "/rest/api/content/1149862011"
    },
    "_links": {
      "webui": "/spaces/~323528440",
      "self": "https://dalstonsemantics.atlassian.net/wiki/rest/api/space/~323528440"
    }
  },
  "history": {
    "latest": true,
    "createdBy": {
      "type": "known",
      "accountId": "557058:d092c978-5ab7-4c8a-8d87-32ffac22c584",
      "accountType": "atlassian",
      "email": "eugene.morozov@dalstonsemantics.com",
      "publicName": "Eugene Morozov",
      "profilePicture": {
        "path": "/wiki/aa-avatar/557058:d092c978-5ab7-4c8a-8d87-32ffac22c584",
        "width": 48,
        "height": 48,
        "isDefault": false
      },
      "displayName": "Eugene Morozov",
      "isExternalCollaborator": false,
      "_expandable": {
        "operations": "",
        "personalSpace": ""
      },
      "_links": {
        "self": "https://dalstonsemantics.atlassian.net/wiki/rest/api/user?accountId=557058:d092c978-5ab7-4c8a-8d87-32ffac22c584"
      }
    },
    "createdDate": "2021-09-14T07:39:42.016Z",
    "_expandable": {
      "lastUpdated": "",
      "previousVersion": "",
      "contributors": "",
      "nextVersion": ""
    },
    "_links": {
      "self": "https://dalstonsemantics.atlassian.net/wiki/rest/api/content/1188593679/history"
    }
  },
  "version": {
    "by": {
      "type": "known",
      "accountId": "557058:d092c978-5ab7-4c8a-8d87-32ffac22c584",
      "accountType": "atlassian",
      "email": "eugene.morozov@dalstonsemantics.com",
      "publicName": "Eugene Morozov",
      "profilePicture": {
        "path": "/wiki/aa-avatar/557058:d092c978-5ab7-4c8a-8d87-32ffac22c584",
        "width": 48,
        "height": 48,
        "isDefault": false
      },
      "displayName": "Eugene Morozov",
      "isExternalCollaborator": false,
      "_expandable": {
        "operations": "",
        "personalSpace": ""
      },
      "_links": {
        "self": "https://dalstonsemantics.atlassian.net/wiki/rest/api/user?accountId=557058:d092c978-5ab7-4c8a-8d87-32ffac22c584"
      }
    },
    "when": "2021-09-14T07:50:42.122Z",
    "friendlyWhen": "just a moment ago",
    "message": "",
    "number": 2,
    "minorEdit": false,
    "contentTypeModified": false,
    "_expandable": {
      "collaborators": "",
      "content": "/rest/api/content/1188593679"
    },
    "_links": {
      "self": "https://dalstonsemantics.atlassian.net/wiki/rest/api/content/1188593679/version/2"
    }
  },
  "macroRenderedOutput": {},
  "metadata": {
    "labels": {
      "results": [],
      "start": 0,
      "limit": 200,
      "size": 0,
      "_links": {
        "next": "/rest/api/content/1188593679/label?next=true&limit=200&start=200",
        "self": "https://dalstonsemantics.atlassian.net/wiki/rest/api/content/1188593679/label"
      }
    },
    "_expandable": {
      "currentuser": "",
      "comments": "",
      "simple": "",
      "properties": "",
      "frontend": "",
      "likes": ""
    }
  },
  "_expandable": {
    "childTypes": "",
    "container": "/rest/api/content/1188560897",
    "operations": "",
    "schedulePublishDate": "",
    "children": "/rest/api/content/1188593679/child",
    "restrictions": "/rest/api/content/1188593679/restriction/byOperation",
    "ancestors": "",
    "body": "",
    "descendants": "/rest/api/content/1188593679/descendant"
  },
  "_links": {
    "context": "/wiki",
    "self": "https://dalstonsemantics.atlassian.net/wiki/rest/api/content/1188593679",
    "collection": "/rest/api/content",
    "webui": "",
    "base": "https://dalstonsemantics.atlassian.net/wiki"
  }
}
```

### Removal of property

Event: `content_removed`

Via `DELETE` to `https://dalstonsemantics.atlassian.net/wiki/rest/api/content/1188593679`.

#### Body

```
{
  "id": "1188593679",
  "type": "com.atlassian.confluence.plugins.confluence-content-property-storage:content-property",
  "status": "current",
  "title": "subject",
  "space": {
    "id": 1149861892,
    "key": "~323528440",
    "name": "Eugene Morozov",
    "type": "personal",
    "status": "current",
    "_expandable": {
      "settings": "/rest/api/space/~323528440/settings",
      "metadata": "",
      "operations": "",
      "lookAndFeel": "/rest/api/settings/lookandfeel?spaceKey=~323528440",
      "identifiers": "",
      "permissions": "",
      "icon": "",
      "description": "",
      "theme": "/rest/api/space/~323528440/theme",
      "history": "",
      "homepage": "/rest/api/content/1149862011"
    },
    "_links": {
      "webui": "/spaces/~323528440",
      "self": "https://dalstonsemantics.atlassian.net/wiki/rest/api/space/~323528440"
    }
  },
  "history": {
    "latest": true,
    "createdBy": {
      "type": "known",
      "accountId": "557058:d092c978-5ab7-4c8a-8d87-32ffac22c584",
      "accountType": "atlassian",
      "email": "eugene.morozov@dalstonsemantics.com",
      "publicName": "Eugene Morozov",
      "profilePicture": {
        "path": "/wiki/aa-avatar/557058:d092c978-5ab7-4c8a-8d87-32ffac22c584",
        "width": 48,
        "height": 48,
        "isDefault": false
      },
      "displayName": "Eugene Morozov",
      "isExternalCollaborator": false,
      "_expandable": {
        "operations": "",
        "personalSpace": ""
      },
      "_links": {
        "self": "https://dalstonsemantics.atlassian.net/wiki/rest/api/user?accountId=557058:d092c978-5ab7-4c8a-8d87-32ffac22c584"
      }
    },
    "createdDate": "2021-09-14T07:39:42.016Z",
    "_expandable": {
      "lastUpdated": "",
      "previousVersion": "",
      "contributors": "",
      "nextVersion": ""
    },
    "_links": {
      "self": "https://dalstonsemantics.atlassian.net/wiki/rest/api/content/1188593679/history"
    }
  },
  "version": {
    "by": {
      "type": "known",
      "accountId": "557058:d092c978-5ab7-4c8a-8d87-32ffac22c584",
      "accountType": "atlassian",
      "email": "eugene.morozov@dalstonsemantics.com",
      "publicName": "Eugene Morozov",
      "profilePicture": {
        "path": "/wiki/aa-avatar/557058:d092c978-5ab7-4c8a-8d87-32ffac22c584",
        "width": 48,
        "height": 48,
        "isDefault": false
      },
      "displayName": "Eugene Morozov",
      "isExternalCollaborator": false,
      "_expandable": {
        "operations": "",
        "personalSpace": ""
      },
      "_links": {
        "self": "https://dalstonsemantics.atlassian.net/wiki/rest/api/user?accountId=557058:d092c978-5ab7-4c8a-8d87-32ffac22c584"
      }
    },
    "when": "2021-09-14T07:50:42.122Z",
    "friendlyWhen": "30 minutes ago",
    "message": "",
    "number": 2,
    "minorEdit": false,
    "contentTypeModified": false,
    "_expandable": {
      "collaborators": "",
      "content": "/rest/api/content/1188593679"
    },
    "_links": {
      "self": "https://dalstonsemantics.atlassian.net/wiki/rest/api/content/1188593679/version/2"
    }
  },
  "macroRenderedOutput": {},
  "metadata": {
    "labels": {
      "results": [],
      "start": 0,
      "limit": 200,
      "size": 0,
      "_links": {
        "next": "/rest/api/content/1188593679/label?next=true&limit=200&start=200",
        "self": "https://dalstonsemantics.atlassian.net/wiki/rest/api/content/1188593679/label"
      }
    },
    "_expandable": {
      "currentuser": "",
      "comments": "",
      "simple": "",
      "properties": "",
      "frontend": "",
      "likes": ""
    }
  },
  "_expandable": {
    "childTypes": "",
    "container": "/rest/api/space/~323528440",
    "operations": "",
    "schedulePublishDate": "",
    "children": "/rest/api/content/1188593679/child",
    "restrictions": "/rest/api/content/1188593679/restriction/byOperation",
    "ancestors": "",
    "body": "",
    "descendants": "/rest/api/content/1188593679/descendant"
  },
  "_links": {
    "context": "/wiki",
    "self": "https://dalstonsemantics.atlassian.net/wiki/rest/api/content/1188593679",
    "collection": "/rest/api/content",
    "webui": "",
    "base": "https://dalstonsemantics.atlassian.net/wiki"
  }
}
```

### Page updated

Event: `page_updated`

For example update to title, etc.

#### Body

```
{
  "userAccountId": "557058:d092c978-5ab7-4c8a-8d87-32ffac22c584",
  "accountType": "customer",
  "updateTrigger": "edit_page",
  "page": {
    "creatorAccountId": "557058:d092c978-5ab7-4c8a-8d87-32ffac22c584",
    "spaceKey": "~323528440",
    "modificationDate": 1631606188966,
    "lastModifierAccountId": "557058:d092c978-5ab7-4c8a-8d87-32ffac22c584",
    "self": "https://dalstonsemantics.atlassian.net/wiki/spaces/~323528440/pages/1188560897/Telstra+Stuff",
    "id": 1188560897,
    "title": "Telstra Stuff",
    "creationDate": 1631605030794,
    "contentType": "page",
    "version": 2
  },
  "timestamp": 1631606188983
}
```

### Page deleted (moved to Trash)

Event: `page_trashed`

#### Body

```
{
  "userAccountId": "557058:d092c978-5ab7-4c8a-8d87-32ffac22c584",
  "accountType": "customer",
  "page": {
    "creatorAccountId": "557058:d092c978-5ab7-4c8a-8d87-32ffac22c584",
    "spaceKey": "~323528440",
    "modificationDate": 1631606188966,
    "lastModifierAccountId": "557058:d092c978-5ab7-4c8a-8d87-32ffac22c584",
    "self": "https://dalstonsemantics.atlassian.net/wiki/spaces/~323528440/pages/1188560897/Telstra+Stuff",
    "id": 1188560897,
    "title": "Telstra Stuff",
    "creationDate": 1631605030794,
    "contentType": "page",
    "version": 2
  },
  "timestamp": 1631606688675
}
```

### Page purged from Trash

Event: `page_removed`

#### Body

```
{
  "userAccountId": "557058:d092c978-5ab7-4c8a-8d87-32ffac22c584",
  "accountType": "customer",
  "page": {
    "creatorAccountId": "557058:d092c978-5ab7-4c8a-8d87-32ffac22c584",
    "spaceKey": "~323528440",
    "modificationDate": 1631276176556,
    "lastModifierAccountId": "557058:d092c978-5ab7-4c8a-8d87-32ffac22c584",
    "self": "https://dalstonsemantics.atlassian.net/wiki/spaces/~323528440/pages/1183744007/BHP",
    "id": 1183744007,
    "title": "BHP",
    "creationDate": 1631276139308,
    "contentType": "page",
    "version": 1
  },
  "timestamp": 1631606984673
}
```

### Page restored from Trash

Event: `page_restored`

#### Body

```
{
  "userAccountId": "557058:d092c978-5ab7-4c8a-8d87-32ffac22c584",
  "accountType": "customer",
  "page": {
    "creatorAccountId": "557058:d092c978-5ab7-4c8a-8d87-32ffac22c584",
    "spaceKey": "~323528440",
    "modificationDate": 1631607176651,
    "lastModifierAccountId": "557058:d092c978-5ab7-4c8a-8d87-32ffac22c584",
    "self": "https://dalstonsemantics.atlassian.net/wiki/spaces/~323528440/pages/1188560897/Telstra+Stuff",
    "id": 1188560897,
    "title": "Telstra Stuff",
    "creationDate": 1631605030794,
    "contentType": "page",
    "version": 2
  },
  "timestamp": 1631612951289
}
```

### Page restored from Archive

Event `page_moved`

#### Body

```
{
  "userAccountId": "557058:d092c978-5ab7-4c8a-8d87-32ffac22c584",
  "accountType": "customer",
  "page": {
    "creatorAccountId": "557058:d092c978-5ab7-4c8a-8d87-32ffac22c584",
    "spaceKey": "~323528440",
    "modificationDate": 1631607176651,
    "lastModifierAccountId": "557058:d092c978-5ab7-4c8a-8d87-32ffac22c584",
    "self": "https://dalstonsemantics.atlassian.net/wiki/spaces/~323528440/pages/1188560897/Telstra+Stuff",
    "id": 1188560897,
    "title": "Telstra Stuff",
    "creationDate": 1631605030794,
    "contentType": "page",
    "version": 2
  },
  "timestamp": 1631612951289
}
```

## REST calls to retrive content

Get body of the content:

```
https://dalstonsemantics.atlassian.net/wiki/rest/api/content/1202061396/version/21?expand=content.body.storage
```

Retrieve macro data:

```
GET https://dalstonsemantics.atlassian.net/wiki/rest/api/content/1202061396/history/21/macro/id/eb59fcc8-dfb6-40f8-9b22-18f743ed4818
```