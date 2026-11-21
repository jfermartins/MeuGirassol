package com.meugirassol.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import android.widget.RadioGroup;

public class MainActivity extends Activity {
    private static final String PREFS = "meu_girassol_local_v1";
    private static final String PROFILE_KEY = "profile_json";
    private static final int MAX_CUSTOM_FIELDS = 8;
    private static final int REQUEST_PICK_PHOTO = 4101;
    private static final int REQUEST_SAVE_BADGE = 4102;

    private final String[] fieldIds = {"name", "cid", "allergies", "relevant", "support", "contact", "instructions"};
    private final String[] fieldLabels = {
            "Nome ou nome social", "CID", "Alergias", "Outras informações relevantes",
            "O que pode me ajudar", "Contato de confiança", "Orientações importantes"
    };
    private final int[] inputIds = {
            R.id.nameInput, R.id.cidInput, R.id.allergiesInput, R.id.relevantInput,
            R.id.supportInput, R.id.contactInput, R.id.instructionsInput
    };
    private final int[] publicIds = {
            R.id.namePublic, R.id.cidPublic, R.id.allergiesPublic, R.id.relevantPublic,
            R.id.supportPublic, R.id.contactPublic, R.id.instructionsPublic
    };

    private final Map<String, EditText> inputs = new LinkedHashMap<>();
    private final Map<String, CheckBox> publicChecks = new LinkedHashMap<>();
    private final List<CustomFieldRow> customRows = new ArrayList<>();

    private LinearLayout customContainer;
    private LinearLayout editorSection;
    private LinearLayout publicSection;
    private LinearLayout publicFieldsContainer;
    private LinearLayout badgePreview;
    private LinearLayout badgeDetailsContainer;
    private CheckBox consentCheck;
    private ImageView qrImage;
    private ImageView photoThumbnail;
    private ImageView badgePhoto;
    private TextView badgePhotoPlaceholder;
    private Button shareButton;
    private Button saveBadgeButton;
    private byte[] badgePngBytes;
    private Uri selectedPhotoUri;
    private TextView statusText;
    private CheckBox identityPublic;
    private RadioGroup identityGroup;
    private Button createQrButton;
    private TextView badgeIcon, badgeRibbon, badgeFooterText;
    private boolean suppressSaving;
    private boolean identitySelectionConfirmed;
    private String generatedLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        restoreProfile();
        bindActions();
        handleIncomingLink(getIntent());
    }

    private void bindViews() {
        editorSection = findViewById(R.id.editorSection);
        publicSection = findViewById(R.id.publicProfileSection);
        customContainer = findViewById(R.id.customFieldsContainer);
        publicFieldsContainer = findViewById(R.id.publicFieldsContainer);
        badgePreview = findViewById(R.id.badgePreview);
        badgeDetailsContainer = findViewById(R.id.badgeDetailsContainer);
        consentCheck = findViewById(R.id.consentCheck);
        qrImage = findViewById(R.id.qrImage);
        photoThumbnail = findViewById(R.id.photoThumbnail);
        badgePhoto = findViewById(R.id.badgePhoto);
        badgePhotoPlaceholder = findViewById(R.id.badgePhotoPlaceholder);
        shareButton = findViewById(R.id.shareLinkButton);
        saveBadgeButton = findViewById(R.id.saveBadgeButton);
        statusText = findViewById(R.id.statusText);
        identityPublic = findViewById(R.id.identityPublic);
        identityGroup = findViewById(R.id.identityGroup);
        createQrButton = findViewById(R.id.createQrButton);
        badgeIcon = findViewById(R.id.badgeIcon); badgeRibbon = findViewById(R.id.badgeRibbon); badgeFooterText = findViewById(R.id.badgeFooterText);

        for (int i = 0; i < fieldIds.length; i++) {
            inputs.put(fieldIds[i], findViewById(inputIds[i]));
            publicChecks.put(fieldIds[i], findViewById(publicIds[i]));
        }
    }

    private void bindActions() {
        findViewById(R.id.addCustomButton).setOnClickListener(view -> {
            if (customRows.size() >= MAX_CUSTOM_FIELDS) {
                Toast.makeText(this, "Você pode adicionar até 8 campos personalizados.", Toast.LENGTH_SHORT).show();
                return;
            }
            addCustomRow("", "", false);
            handleProfileEdit();
        });

        createQrButton.setOnClickListener(view -> createQr());
        findViewById(R.id.choosePhotoButton).setOnClickListener(view -> choosePhoto());
        saveBadgeButton.setOnClickListener(view -> saveBadgeAsPng());
        findViewById(R.id.shareLinkButton).setOnClickListener(view -> shareProfileLink());
        findViewById(R.id.backToEditButton).setOnClickListener(view -> showEditor());
        findViewById(R.id.deleteButton).setOnClickListener(view -> confirmDelete());
    }

    private void restoreProfile() {
        suppressSaving = true;
        String saved = getSharedPreferences(PREFS, MODE_PRIVATE).getString(PROFILE_KEY, "{}");
        try {
            JSONObject profile = new JSONObject(saved);
            JSONObject fields = profile.optJSONObject("fields");
            JSONObject selection = profile.optJSONObject("publicSelection");
            for (String id : fieldIds) {
                EditText input = inputs.get(id);
                if (fields != null) input.setText(fields.optString(id, ""));
                CheckBox check = publicChecks.get(id);
                if (selection != null) {
                    check.setChecked(selection.optBoolean(id, id.equals("name")));
                } else {
                    check.setChecked(id.equals("name"));
                }
            }
            String identity = profile.optString("identity", "");
            identitySelectionConfirmed = profile.optBoolean("identitySelected", false);
            if (identitySelectionConfirmed && "autism".equals(identity)) identityGroup.check(R.id.identityAutism);
            else if (identitySelectionConfirmed && "sunflower".equals(identity)) identityGroup.check(R.id.identitySunflower);
            else { identitySelectionConfirmed = false; identityGroup.clearCheck(); }
            identityPublic.setChecked(profile.optBoolean("identityPublic", false));
            JSONArray customs = profile.optJSONArray("customFields");
            if (customs != null) {
                for (int i = 0; i < customs.length() && i < MAX_CUSTOM_FIELDS; i++) {
                    JSONObject field = customs.optJSONObject(i);
                    if (field != null) {
                        addCustomRow(field.optString("label", ""), field.optString("value", ""), field.optBoolean("isPublic", false));
                    }
                }
            }
        } catch (JSONException ignored) {
            // Um arquivo local inválido não impede o início do aplicativo.
        }
        suppressSaving = false;
        attachAutosave();
        identityGroup.setOnCheckedChangeListener((group, checkedId) -> { identitySelectionConfirmed = checkedId == R.id.identityAutism || checkedId == R.id.identitySunflower; updateBadgeTheme(); handleProfileEdit(); });
        identityPublic.setOnCheckedChangeListener((button, checked) -> handleProfileEdit());
        updateBadgeTheme();

        String savedPhoto = getSharedPreferences(PREFS, MODE_PRIVATE).getString("photo_uri", null);
        if (savedPhoto != null) {
            try {
                selectedPhotoUri = Uri.parse(savedPhoto);
                loadSelectedPhoto();
            } catch (RuntimeException ignored) {
                selectedPhotoUri = null;
            }
        }

        String priorLink = getSharedPreferences(PREFS, MODE_PRIVATE).getString("last_qr_link", null);
        if (priorLink != null && priorLink.startsWith("meugirassol://perfil?d=")) {
            try {
                qrImage.setImageBitmap(encodeQr(priorLink));
                qrImage.setVisibility(View.VISIBLE);
                badgePreview.setVisibility(View.VISIBLE);
                saveBadgeButton.setVisibility(View.VISIBLE);
                shareButton.setVisibility(View.VISIBLE);
                generatedLink = priorLink;
                try {
                    JSONObject shared = buildSharedData();
                    if (shared != null) updateBadgeDetails(shared);
                } catch (JSONException ignored) { }
                statusText.setText("QR Code offline salvo neste dispositivo. Os dados selecionados estão no próprio QR.");
            } catch (WriterException ignored) {
                getSharedPreferences(PREFS, MODE_PRIVATE).edit().remove("last_qr_link").apply();
            }
        }
    }

    private void choosePhoto() {
        Intent picker = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        picker.addCategory(Intent.CATEGORY_OPENABLE);
        picker.setType("image/*");
        picker.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(picker, REQUEST_PICK_PHOTO);
    }

    private void loadSelectedPhoto() {
        if (selectedPhotoUri == null) return;
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (java.io.InputStream boundsStream = getContentResolver().openInputStream(selectedPhotoUri)) {
                BitmapFactory.decodeStream(boundsStream, null, bounds);
            }
            int sampleSize = 1;
            while (bounds.outWidth / sampleSize > 1600 || bounds.outHeight / sampleSize > 1600) sampleSize *= 2;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize;
            Bitmap photo;
            try (java.io.InputStream imageStream = getContentResolver().openInputStream(selectedPhotoUri)) {
                photo = BitmapFactory.decodeStream(imageStream, null, options);
            }
            if (photo == null) throw new IllegalArgumentException("Imagem inválida");
            photoThumbnail.setImageBitmap(photo);
            badgePhoto.setImageBitmap(photo);
            badgePhotoPlaceholder.setVisibility(View.GONE);
        } catch (Exception exception) {
            Toast.makeText(this, "Não foi possível abrir essa foto. Escolha outra imagem.", Toast.LENGTH_LONG).show();
        }
    }

    private void saveBadgeAsPng() {
        if (badgePreview.getVisibility() != View.VISIBLE || generatedLink == null) {
            statusText.setText("Crie o QR Code antes de salvar o crachá.");
            return;
        }
        badgePreview.post(() -> {
            int width = badgePreview.getWidth();
            int height = badgePreview.getHeight();
            if (width <= 0 || height <= 0) {
                statusText.setText("Aguarde a prévia do crachá aparecer e tente novamente.");
                return;
            }
            final int scale = Math.min(3, Math.max(2, 2400 / width));
            Bitmap bitmap = Bitmap.createBitmap(width * scale, height * scale, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);
            canvas.scale(scale, scale);
            badgePreview.draw(canvas);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
            badgePngBytes = output.toByteArray();
            bitmap.recycle();

            Intent createFile = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            createFile.addCategory(Intent.CATEGORY_OPENABLE);
            createFile.setType("image/png");
            createFile.putExtra(Intent.EXTRA_TITLE, "meu-girassol-cracha.png");
            startActivityForResult(createFile, REQUEST_SAVE_BADGE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();

        if (requestCode == REQUEST_PICK_PHOTO) {
            try {
                getContentResolver().takePersistableUriPermission(uri,
                        data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException ignored) { }
            selectedPhotoUri = uri;
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString("photo_uri", uri.toString()).apply();
            loadSelectedPhoto();
            if (generatedLink != null) {
                badgePreview.setVisibility(View.VISIBLE);
                saveBadgeButton.setVisibility(View.VISIBLE);
            }
            statusText.setText("Foto adicionada ao crachá. Ela não foi incluída no QR Code.");
            return;
        }

        if (requestCode == REQUEST_SAVE_BADGE && badgePngBytes != null) {
            try (OutputStream stream = getContentResolver().openOutputStream(uri, "w")) {
                if (stream == null) throw new java.io.IOException("Não foi possível abrir o arquivo");
                stream.write(badgePngBytes);
                stream.flush();
                statusText.setText("Crachá PNG salvo. Você pode imprimir a imagem.");
            } catch (Exception exception) {
                statusText.setText("Não foi possível salvar o crachá. Tente escolher outra pasta.");
            }
        }
    }

    private void updateBadgeDetails(JSONObject shared) {
        badgeDetailsContainer.removeAllViews();
        JSONObject fields = shared.optJSONObject("fields");
        String[] order = {"name", "cid", "contact", "allergies", "support", "relevant", "instructions"};
        int added = 0;
        for (String id : order) {
            if (added >= 4 || fields == null) break;
            String value = fields.optString(id, "").trim();
            if (!value.isEmpty()) {
                addBadgeDetail(fieldLabel(id), value);
                added++;
            }
        }
        JSONArray custom = shared.optJSONArray("custom");
        if (custom != null) {
            for (int i = 0; i < custom.length() && added < 4; i++) {
                JSONObject field = custom.optJSONObject(i);
                if (field != null) {
                    String label = field.optString("label", "").trim();
                    String value = field.optString("value", "").trim();
                    if (!label.isEmpty() && !value.isEmpty()) {
                        addBadgeDetail(label, value);
                        added++;
                    }
                }
            }
        }
        if (added == 0) addBadgeDetail("MEU PERFIL", "Leia o QR para saber como me apoiar.");
    }

    private String fieldLabel(String id) {
        for (int i = 0; i < fieldIds.length; i++) {
            if (fieldIds[i].equals(id)) return fieldLabels[i];
        }
        return "Informação";
    }

    private void addBadgeDetail(String labelText, String valueText) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setBackgroundResource(R.drawable.badge_detail_value);
        item.setPadding(dp(4), dp(2), dp(4), dp(2));
        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        itemParams.bottomMargin = dp(3);
        badgeDetailsContainer.addView(item, itemParams);

        TextView label = new TextView(this);
        label.setText(labelText.toUpperCase(java.util.Locale.ROOT));
        label.setTextColor(Color.WHITE);
        label.setTextSize(7);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        label.setSingleLine(true);
        label.setEllipsize(android.text.TextUtils.TruncateAt.END);
        label.setPadding(dp(4), dp(1), dp(4), dp(1));
        label.setBackgroundResource(R.drawable.badge_detail_label);
        item.addView(label, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView value = new TextView(this);
        value.setText(valueText.replace('\n', ' '));
        value.setTextColor(Color.rgb(39, 53, 46));
        value.setTextSize(9);
        value.setMaxLines(2);
        value.setEllipsize(android.text.TextUtils.TruncateAt.END);
        value.setPadding(dp(3), dp(2), dp(3), dp(1));
        item.addView(value, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void attachAutosave() {
        for (EditText input : inputs.values()) {
            input.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { handleProfileEdit(); }
                @Override public void afterTextChanged(android.text.Editable s) { }
            });
        }
        for (CheckBox check : publicChecks.values()) {
            check.setOnCheckedChangeListener((button, checked) -> handleProfileEdit());
        }
    }

    private void addCustomRow(String label, String value, boolean isPublic) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(10), dp(8), dp(10), dp(8));
        card.setBackgroundResource(R.drawable.input_background);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.topMargin = dp(10);
        customContainer.addView(card, cardParams);

        EditText labelInput = new EditText(this);
        labelInput.setSingleLine(true);
        labelInput.setHint("Nome do campo (ex.: Comunicação)");
        labelInput.setText(label);
        labelInput.setTextSize(14);
        labelInput.setPadding(dp(8), dp(6), dp(8), dp(6));
        card.addView(labelInput, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        EditText valueInput = new EditText(this);
        valueInput.setHint("Escreva aqui, se desejar...");
        valueInput.setText(value);
        valueInput.setTextSize(14);
        valueInput.setMinLines(2);
        valueInput.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        valueInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        valueInput.setPadding(dp(8), dp(6), dp(8), dp(6));
        card.addView(valueInput, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        CheckBox publicCheck = new CheckBox(this);
        publicCheck.setText("Incluir este campo no QR Code");
        publicCheck.setTextSize(12);
        publicCheck.setButtonTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.forest)));
        publicCheck.setChecked(isPublic);
        card.addView(publicCheck, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Button removeButton = new Button(this);
        removeButton.setText("Remover campo");
        removeButton.setAllCaps(false);
        card.addView(removeButton, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        CustomFieldRow row = new CustomFieldRow(card, labelInput, valueInput, publicCheck);
        customRows.add(row);
        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { handleProfileEdit(); }
            @Override public void afterTextChanged(android.text.Editable s) { }
        };
        labelInput.addTextChangedListener(watcher);
        valueInput.addTextChangedListener(watcher);
        publicCheck.setOnCheckedChangeListener((button, checked) -> handleProfileEdit());
        removeButton.setOnClickListener(view -> {
            customRows.remove(row);
            customContainer.removeView(card);
            handleProfileEdit();
        });
    }

    private void handleProfileEdit() {
        if (suppressSaving) return;
        consentCheck.setChecked(false);
        generatedLink = null;
        qrImage.setImageDrawable(null);
        badgePreview.setVisibility(View.GONE);
        saveBadgeButton.setVisibility(View.GONE);
        shareButton.setVisibility(View.GONE);
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().remove("last_qr_link").apply();
        statusText.setText("Perfil alterado. Revise o consentimento e gere um novo QR offline.");
        saveProfile();
    }

    private void saveProfile() {
        if (suppressSaving || inputs.isEmpty()) return;
        try {
            JSONObject root = new JSONObject();
            JSONObject fields = new JSONObject();
            JSONObject selection = new JSONObject();
            for (String id : fieldIds) {
                fields.put(id, inputs.get(id).getText().toString());
                selection.put(id, publicChecks.get(id).isChecked());
            }
            JSONArray customs = new JSONArray();
            for (CustomFieldRow row : customRows) {
                JSONObject field = new JSONObject();
                field.put("label", row.label.getText().toString());
                field.put("value", row.value.getText().toString());
                field.put("isPublic", row.isPublic.isChecked());
                customs.put(field);
            }
            int selectedIdentity = identityGroup.getCheckedRadioButtonId();
            root.put("identity", selectedIdentity == R.id.identityAutism ? "autism" : selectedIdentity == R.id.identitySunflower ? "sunflower" : "");
            root.put("identitySelected", identitySelectionConfirmed && selectedIdentity != -1);
            root.put("identityPublic", identityPublic.isChecked());
            root.put("fields", fields);
            root.put("publicSelection", selection);
            root.put("customFields", customs);
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PROFILE_KEY, root.toString()).apply();
        } catch (JSONException ignored) {
            statusText.setText("Não foi possível salvar agora.");
        }
    }

    private JSONObject buildSharedData() throws JSONException {
        JSONObject sharedFields = new JSONObject();
        int count = 0;
        for (String id : fieldIds) {
            String value = inputs.get(id).getText().toString().trim();
            if (publicChecks.get(id).isChecked() && !value.isEmpty()) {
                sharedFields.put(id, value);
                count++;
            }
        }
        JSONArray sharedCustom = new JSONArray();
        for (CustomFieldRow row : customRows) {
            String label = row.label.getText().toString().trim();
            String value = row.value.getText().toString().trim();
            if (row.isPublic.isChecked() && !label.isEmpty() && !value.isEmpty()) {
                JSONObject field = new JSONObject();
                field.put("label", label);
                field.put("value", value);
                sharedCustom.put(field);
                count++;
            }
        }
        if (count == 0 && !identityPublic.isChecked()) return null;
        JSONObject data = new JSONObject();
        data.put("fields", sharedFields);
        data.put("custom", sharedCustom);
        data.put("identity", identityPublic.isChecked() ? (identityGroup.getCheckedRadioButtonId() == R.id.identityAutism ? "autism" : "sunflower") : JSONObject.NULL);
        return data;
    }

    private void createQr() {
        saveProfile();
        if (!identitySelectionConfirmed || identityGroup.getCheckedRadioButtonId() == -1) { statusText.setText("Escolha explicitamente o tema do crachá: Autismo ou Outra deficiência oculta (girassol)."); return; }
        if (!consentCheck.isChecked()) { statusText.setText("Confirme que entendeu: os campos selecionados ficam dentro do QR, podem ser lidos por qualquer pessoa e não são criptografados. A foto não vai no QR."); return; }
        try {
            JSONObject data = buildSharedData();
            if (data == null) { statusText.setText("Marque ao menos uma informação ou autorize a categoria para incluir no QR."); return; }
            if (data.optJSONObject("fields").length() == 0 && data.optJSONArray("custom").length() == 0 && data.isNull("identity")) { statusText.setText("Marque ao menos uma informação ou a categoria para incluir no QR."); return; }
            String token = Base64.encodeToString(data.toString().getBytes(StandardCharsets.UTF_8), Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
            String localQr = "meugirassol://perfil?d=" + token;
            if (localQr.length() > 2200) { statusText.setText("Há informações demais para caber neste QR. Remova textos longos ou campos e tente novamente."); return; }
            generatedLink = localQr;
            qrImage.setImageBitmap(encodeQr(localQr));
            qrImage.setVisibility(View.VISIBLE);
            updateBadgeDetails(data);
            updateBadgeTheme();
            badgePreview.setVisibility(View.VISIBLE);
            saveBadgeButton.setVisibility(View.VISIBLE);
            shareButton.setVisibility(View.VISIBLE);
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString("last_qr_link", localQr).apply();
            statusText.setText("QR offline criado. Os campos selecionados estão gravados no próprio QR; qualquer pessoa que o escanear poderá lê-los. Foto mantida neste aparelho.");
        } catch (Exception e) { statusText.setText("Não foi possível preparar o QR offline. Revise os dados selecionados e tente novamente."); }
    }
    private void updateBadgeTheme() {
        if (badgeIcon == null) return;
        int selection = identityGroup.getCheckedRadioButtonId();
        if (selection == R.id.identityAutism) {
            badgeIcon.setText("🧩"); badgeRibbon.setText("IDENTIFICAÇÃO DO AUTISMO");
            badgeFooterText.setText("Respeite meu tempo e minhas formas de comunicação.");
        } else if (selection == R.id.identitySunflower) {
            badgeIcon.setText("✿"); badgeRibbon.setText("CORDÃO DE GIRASSOL");
            badgeFooterText.setText("Meu cordão de girassol sinaliza uma deficiência não aparente. INCLUSÃO É RESPEITO!");
        } else {
            badgeIcon.setText("?"); badgeRibbon.setText("ESCOLHA UMA IDENTIFICAÇÃO");
            badgeFooterText.setText("Selecione o tema do crachá antes de criar o QR.");
        }
    }

    private Bitmap encodeQr(String value) throws WriterException {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 2);
        BitMatrix matrix = new QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, 720, 720, hints);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = matrix.get(x, y) ? Color.rgb(39, 53, 46) : Color.WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private void shareProfileLink() {
        if (generatedLink == null) return;
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, generatedLink);
        startActivity(Intent.createChooser(send, "Compartilhar código offline do perfil"));
    }

    private void handleIncomingLink(Intent intent) {
        Uri uri = intent == null ? null : intent.getData();
        if (uri == null || !"meugirassol".equals(uri.getScheme()) || !"perfil".equals(uri.getHost())) return;
        String token = uri.getQueryParameter("d");
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Este QR não contém um perfil válido.", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            byte[] bytes = Base64.decode(token, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
            JSONObject data = new JSONObject(new String(bytes, StandardCharsets.UTF_8));
            showPublicProfile(data);
        } catch (IllegalArgumentException | JSONException exception) {
            Toast.makeText(this, "Não foi possível ler as informações deste QR.", Toast.LENGTH_LONG).show();
        }
    }

    private void showPublicProfile(JSONObject data) {
        editorSection.setVisibility(View.GONE);
        publicSection.setVisibility(View.VISIBLE);
        publicFieldsContainer.removeAllViews();
        JSONObject fields = data.optJSONObject("fields");
        String name = fields == null ? "" : fields.optString("name", "").trim();
        TextView nameView = findViewById(R.id.publicName);
        nameView.setText(name.isEmpty() ? "Meu perfil de apoio" : name);

        int visible = 0;
        String identity = data.optString("identity", "");
        if ("autism".equals(identity)) {
            addPublicField("Identificação", "Autismo");
            visible++;
        } else if ("sunflower".equals(identity)) {
            addPublicField("Identificação", "Outra deficiência oculta — cordão de girassol");
            visible++;
        }
        if (fields != null) {
            for (int i = 0; i < fieldIds.length; i++) {
                if ("name".equals(fieldIds[i])) continue;
                String value = fields.optString(fieldIds[i], "").trim();
                if (!value.isEmpty()) {
                    addPublicField(fieldLabels[i], value);
                    visible++;
                }
            }
        }
        JSONArray custom = data.optJSONArray("custom");
        if (custom != null) {
            for (int i = 0; i < custom.length(); i++) {
                JSONObject field = custom.optJSONObject(i);
                if (field == null) continue;
                String label = field.optString("label", "").trim();
                String value = field.optString("value", "").trim();
                if (!label.isEmpty() && !value.isEmpty()) {
                    addPublicField(label, value);
                    visible++;
                }
            }
        }
        if (visible == 0 && name.isEmpty()) {
            addPublicField("Informação", "A pessoa compartilhou seu perfil de apoio.");
        }
        findViewById(R.id.rootScroll).scrollTo(0, 0);
    }

    private void addPublicField(String label, String value) {
        TextView heading = new TextView(this);
        heading.setText(label);
        heading.setTextColor(getColor(R.color.forest));
        heading.setTextSize(13);
        heading.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams headingParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        headingParams.topMargin = dp(8);
        publicFieldsContainer.addView(heading, headingParams);

        TextView content = new TextView(this);
        content.setText(value);
        content.setTextColor(getColor(R.color.ink));
        content.setTextSize(16);
        content.setPadding(0, dp(3), 0, dp(7));
        publicFieldsContainer.addView(content, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void showEditor() {
        publicSection.setVisibility(View.GONE);
        editorSection.setVisibility(View.VISIBLE);
        findViewById(R.id.rootScroll).scrollTo(0, 0);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Apagar informações?")
                .setMessage("Isso apaga o perfil, a foto e o QR guardados neste aparelho. QR Codes que já foram salvos, impressos ou enviados não podem ser apagados por este aplicativo.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Apagar", (dialog, which) -> clearLocalData())
                .show();
    }
    private void clearLocalData() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().clear().apply(); suppressSaving=true;
        for(EditText input:inputs.values())input.setText(""); for(int i=0;i<fieldIds.length;i++)publicChecks.get(fieldIds[i]).setChecked(fieldIds[i].equals("name"));
        customRows.clear();customContainer.removeAllViews();selectedPhotoUri=null;photoThumbnail.setImageDrawable(null);badgePhoto.setImageDrawable(null);badgePhotoPlaceholder.setVisibility(View.VISIBLE);qrImage.setImageDrawable(null);badgePreview.setVisibility(View.GONE);saveBadgeButton.setVisibility(View.GONE);shareButton.setVisibility(View.GONE);generatedLink=null;badgePngBytes=null;consentCheck.setChecked(false);identityGroup.clearCheck();identitySelectionConfirmed=false;identityPublic.setChecked(false);updateBadgeTheme();suppressSaving=false;saveProfile();createQrButton.setEnabled(true);statusText.setText("Dados locais e QR apagados. Cópias já impressas ou compartilhadas não podem ser removidas por este app.");
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingLink(intent);
    }

    private static final class CustomFieldRow {
        final LinearLayout container;
        final EditText label;
        final EditText value;
        final CheckBox isPublic;

        CustomFieldRow(LinearLayout container, EditText label, EditText value, CheckBox isPublic) {
            this.container = container;
            this.label = label;
            this.value = value;
            this.isPublic = isPublic;
        }
    }
}
