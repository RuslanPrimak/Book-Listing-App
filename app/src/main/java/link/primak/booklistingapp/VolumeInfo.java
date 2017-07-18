/*
 * Copyright (c) 2017. Ruslan Primak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Last modified 7/14/17 11:30 PM
 */

package link.primak.booklistingapp;

import android.graphics.Bitmap;

public class VolumeInfo {
    private String author;
    private String title;
    private String link;
    private String id;
    private String smallThumbnail;
    private Bitmap image;

    private VolumeInfo() {

    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getId() {
        return id;
    }

    public String getSmallThumbnail() {
        return smallThumbnail;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "VolumeInfo{" +
                "author='" + author + '\'' +
                ", title='" + title + '\'' +
                ", link='" + link + '\'' +
                '}';
    }

    public static class Builder {
        private String author;
        private String title;
        private String link;
        private String id;
        private String smallThumbnail;

        public Builder() {

        }

        public Builder reset() {
            author = null;
            title = null;
            link = null;
            id = null;
            smallThumbnail = null;
            return this;
        }

        public Builder setAuthor(String author) {
            this.author = author;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setLink(String link) {
            this.link = link;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setSmallThumbnail(String smallThumbnail) {
            this.smallThumbnail = smallThumbnail;
            return this;
        }

        public VolumeInfo build() {
            VolumeInfo result = new VolumeInfo();
            result.author = author;
            result.title = title;
            result.link = link;
            result.id = id;
            result.smallThumbnail = smallThumbnail;

            return result;
        }
    }
}
