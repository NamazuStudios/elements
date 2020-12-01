package com.namazustudios.socialengine.rt.manifest.http;

import com.namazustudios.socialengine.rt.manifest.model.Type;

public class HttpParameter {

        private String name;

        private Type type;

        /**
         * The type of the parameter.
         *
         * @return the String representing the name of the parameter
         */
        public String getName() { return name; }

        /**
         * Sets the name of the parameter
         *
         * @param name of the parameter
         */
        public void setName(String name) { this.name = name; }

        /**
         * The type of the parameter.
         *
         * @return the {@link Type} representing the type of the parameter
         */
        public Type getType() { return type; }

        /**
         * Sets the index of the parameter
         *
         * @param type the {@link Type} of the parameter
         */
        public void setType(Type type) { this.type = type; }

//        // Compare Two HttpParameters based on their index
//        /**
//         * @param   other - The HttpParameter to be compared.
//         * @return  A negative integer, zero, or a positive integer as this parameter
//         *          is less than, equal to, or greater than the supplied object's index.
//         */
//        @Override
//        public int compareTo(HttpParameter other) {
//                return this.getIndex() - other.getIndex();
//        }

        // Two HttpParameters are equal if their indices and types are equal
        @Override
        public boolean equals(Object o) {

                // If the object is compared with itself then return true
                if (o == this) {
                        return true;
                }

                /* Check if o is an instance of HttpParameter or not
                "null instanceof [type]" also returns false */
                if (!(o instanceof HttpParameter)) {
                        return false;
                }

                // typecast o to HttpParameter so that we can compare data members
                HttpParameter c = (HttpParameter) o;

                // Compare the data members and return accordingly
                return this.name == c.name
                        && this.getType() == c.getType();
        }
}
