const { createApp } = Vue;
const { createVuetify } = Vuetify;

const vuetify = createVuetify();

const app = createApp({
    data() {
        return {
            username: 'ITIM Manager',
            password: '1q@3e4r',
            serverUrl: 'http://localhost:8080',
            csrfToken: null,
            error: null,
            loading: false,
            showPassword: false
        }
    },
    methods: {
        async login() {
            this.loading = true;
            this.error = null;
            this.csrfToken = null;

            try {
                const params = new URLSearchParams();
                params.append('username', this.username);
                params.append('password', this.password);

                const response = await axios.post(`${this.serverUrl}/api/auth`, params);
                this.csrfToken = response.data.csrfToken;
            } catch (error) {
                this.error = error.response?.data?.error || 'An error occurred';
            } finally {
                this.loading = false;
            }
        }
    },
    template: `
        <v-app>
            <v-main>
                <v-container>
                    <v-row justify="center">
                        <v-col cols="12" sm="8" md="6" lg="4">
                            <v-card class="mt-10" elevation="8">
                                <v-card-title class="text-center text-h4 py-4">
                                    ISIM Login
                                </v-card-title>
                                <v-card-text>
                                    <v-form @submit.prevent="login">
                                        <v-text-field
                                            v-model="serverUrl"
                                            label="Server URL"
                                            prepend-icon="mdi-server"
                                            variant="outlined"
                                            :rules="[v => !!v || 'Server URL is required']"
                                        ></v-text-field>
                                        
                                        <v-text-field
                                            v-model="username"
                                            label="Username"
                                            prepend-icon="mdi-account"
                                            variant="outlined"
                                            :rules="[v => !!v || 'Username is required']"
                                        ></v-text-field>

                                        <v-text-field
                                            v-model="password"
                                            label="Password"
                                            prepend-icon="mdi-lock"
                                            :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                                            :type="showPassword ? 'text' : 'password'"
                                            variant="outlined"
                                            @click:append="showPassword = !showPassword"
                                            :rules="[v => !!v || 'Password is required']"
                                        ></v-text-field>

                                        <v-alert
                                            v-if="error"
                                            type="error"
                                            class="mb-4"
                                        >
                                            {{ error }}
                                        </v-alert>

                                        <v-alert
                                            v-if="csrfToken"
                                            type="success"
                                            class="mb-4"
                                        >
                                            CSRF Token: {{ csrfToken }}
                                        </v-alert>

                                        <v-btn
                                            block
                                            color="primary"
                                            size="large"
                                            type="submit"
                                            :loading="loading"
                                        >
                                            Login
                                        </v-btn>
                                    </v-form>
                                </v-card-text>
                            </v-card>
                        </v-col>
                    </v-row>
                </v-container>
            </v-main>
        </v-app>
    `
});

app.use(vuetify);
app.mount('#app');
