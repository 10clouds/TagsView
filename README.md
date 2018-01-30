# TagsView

Using tags view you can add and remove new tags

How to use
----------

```gradle
allprojects {
    repositories {
        maven { url 'http://repo.aws.10clouds.com:8081/artifactory/gradle-dev-local/' }
    }
}

dependencies {
    compile(group: 'tenclouds', name: 'tagsview', version: '1.0.0', ext: 'aar')
}
```

Available XML attributes
------------------------

| Attribute name          | Format    | Description                                                                                           |
|-------------------------|-----------|-------------------------------------------------------------------------------------------------------|
| tagsViewEditable        | boolean   | When set to "true" user can add new tags to the view. Otherwise tags can only be set programatically. |
| tagsViewTextSize        | dimension | Text size in tag views and new tag text input view.                                                   |
| tagsViewBackgroundColor | color     | Background color of a tag view.                                                                       |
| tagsViewHint            | string    | Hint displayed in tag input text view. Doesn't show if tagsViewEditable is set to 'false'.            |
| tagsViewAllowDuplicates | boolean   | When set to 'true' two or more of the tags with the same name can be added to the view.               |

Example of usage
----------------

![alt text](https://i.imgur.com/riGBndq.gif)

```
<com.tenclouds.tagsview.TagsView
        android:id="@+id/new_observation_tags_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        app:tagsViewEditable="true"
        app:tagsViewHint="Add your tag"
        app:tagsViewTextSize="12sp" />
```
