# Photo-Album
Photo Album Android app that lets users manage their photo library

#### Tools & Implementation:
- Data Persistance across sessions for all Objects
  * Realm (Realm.io) mobile database for fast queries and writes
  * Implemented Create, Read, Update, Delete (CRUD) for all photos and albums
- Android RecyclerView
  * To display all chosen photos in a given Album with appropriate tags and thumbnail
- Slideshow
  * Display chosen photo in a slideshow
  * Options to add/delete tags, move photo to an Album, delete photo
  * Next/Previous on photos within an Album
- Search
  * filter photos in real-time (through match completion) for a photo in an album via tags
