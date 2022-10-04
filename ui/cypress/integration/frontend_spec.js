describe('The Home Page', () => {
    it('successfully loads without navbar', () => {
        cy.visit('/')
        cy.get('div#navbar').should('not.exist');
    })
})
